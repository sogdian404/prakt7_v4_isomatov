package com.example.prakt7
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BooksCarouselFragment : Fragment(R.layout.fragment_books_carousel) {
    private var _adapter: BookCarouselAdapter? = null
    private val adapter get() = _adapter!!

    private lateinit var database: LibraryDatabase
    private lateinit var spinnerSection: Spinner

    // Текущий выбранный раздел (null = все книги)
    private var currentSection: String? = null

    private val issuedBookIdsFlow = MutableStateFlow<Set<Int>>(emptySet())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = LibraryDatabase.getDatabase(requireContext())
        spinnerSection = view.findViewById(R.id.spinnerSectionFilter)

        viewLifecycleOwner.lifecycleScope.launch {
            DatabaseInitializer.initializeIfNeeded(database)
        }

        // ✅ НАСТРАИВАЕМ ФИЛЬТР ПО РАЗДЕЛАМ
        setupSectionFilter()

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPagerBooks)

        _adapter = BookCarouselAdapter(
            emptyList(),
            onItemClick = { clickedBook ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val isIssued = issuedBookIdsFlow.value.contains(clickedBook.book_id)

                    if (isIssued) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("📚 ${clickedBook.title}")
                            .setMessage("""
                                Автор: ${clickedBook.authorName}
                                Раздел: ${clickedBook.section}
                                Тип: ${if (clickedBook.isElectronic) "Электронная" else "Печатная"}
                                
                                ⚠️ Книга уже выдана!
                            """.trimIndent())
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        val dialogBuilder = AlertDialog.Builder(requireContext())
                            .setTitle("📚 ${clickedBook.title}")
                            .setMessage("""
                                Автор: ${clickedBook.authorName}
                                Раздел: ${clickedBook.section}
                                Тип: ${if (clickedBook.isElectronic) "Электронная" else "Печатная"}
                            """.trimIndent())

                        if (PermissionManager.isLibrarian(requireContext())) {
                            dialogBuilder.setPositiveButton("📤 Выдать книгу") { _, _ ->
                                IssueBookDialogFragment(clickedBook) { }.show(childFragmentManager, "IssueBook")
                            }

                            dialogBuilder.setNeutralButton("✏️ Редактировать") { _, _ ->
                                EditBookDialogFragment(clickedBook) { }.show(childFragmentManager, "EditBook")
                            }
                        }

                        dialogBuilder.setNegativeButton("OK", null).show()
                    }
                }
            },
            onItemLongClick = { bookToDelete ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val isIssued = issuedBookIdsFlow.value.contains(bookToDelete.book_id)
                    if (isIssued) {
                        Toast.makeText(requireContext(), "Нельзя удалить выданную книгу!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    withContext(Dispatchers.IO) {
                        database.bookDao().delete(bookToDelete)
                    }
                    Toast.makeText(requireContext(), "📚 Книга удалена", Toast.LENGTH_SHORT).show()
                }
            }
        )
        viewPager.adapter = adapter

        // Наблюдаем за выданными книгами
        viewLifecycleOwner.lifecycleScope.launch {
            database.loanDao().getActiveLoans().collect { loans ->
                val issuedIds = loans.map { it.book_id }.toSet()
                issuedBookIdsFlow.value = issuedIds
            }
        }

        // ✅ Наблюдаем за книгами С УЧЁТОМ ФИЛЬТРА
        viewLifecycleOwner.lifecycleScope.launch {
            if (currentSection == null) {
                // Все книги
                database.bookDao().getAllBooks().collect { books ->
                    val issuedIds = issuedBookIdsFlow.value
                    val booksWithStatus = books.map { book ->
                        BookWithStatus(book = book, isIssued = book.book_id in issuedIds)
                    }
                    adapter.updateBooksWithStatus(booksWithStatus)
                }
            } else {
                // Книги выбранного раздела
                database.bookDao().getBooksBySection(currentSection!!).collect { books ->
                    val issuedIds = issuedBookIdsFlow.value
                    val booksWithStatus = books.map { book ->
                        BookWithStatus(book = book, isIssued = book.book_id in issuedIds)
                    }
                    adapter.updateBooksWithStatus(booksWithStatus)
                }
            }
        }

        // Настройки карусели
        viewPager.setPageTransformer { page, position ->
            val scale = 1f - kotlin.math.abs(position) * 0.2f
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = 1f - kotlin.math.abs(position) * 0.3f
        }

        viewPager.offscreenPageLimit = 1
        viewPager.clipToPadding = false
        viewPager.clipChildren = false
        viewPager.setPadding(40, 0, 40, 0)

        // Кнопка добавления книги (только для библиотекарей)
        val fabAddBook = view.findViewById<FloatingActionButton>(R.id.fabAddBook)
        if (!PermissionManager.isLibrarian(requireContext())) {
            fabAddBook?.visibility = View.GONE
        }
        fabAddBook?.setOnClickListener {
            AddBookDialogFragment().show(childFragmentManager, "AddBookDialog")
        }
    }

    // ✅ МЕТОД НАСТРОЙКИ ФИЛЬТРА
    private fun setupSectionFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Получаем все уникальные разделы из БД
            val sections = withContext(Dispatchers.IO) {
                database.bookDao().getAllSections()
            }

            // Добавляем опцию "Все разделы"
            val sectionList = mutableListOf("Все разделы")
            sectionList.addAll(sections)

            // Настраиваем Spinner
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sectionList
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSection.adapter = adapter

            // Обработчик выбора раздела
            spinnerSection.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedSection = sectionList[position]

                    if (selectedSection == "Все разделы") {
                        currentSection = null  // Сбрасываем фильтр
                    } else {
                        currentSection = selectedSection  // Устанавливаем фильтр
                    }

                    // Перезапускаем наблюдение за книгами с новым фильтром
                    refreshBooks()
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    currentSection = null
                    refreshBooks()
                }
            }
        }
    }

    // ✅ МЕТОД ОБНОВЛЕНИЯ СПИСКА КНИГ
    private fun refreshBooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (currentSection == null) {
                database.bookDao().getAllBooks().collect { books ->
                    val issuedIds = issuedBookIdsFlow.value
                    val booksWithStatus = books.map { book ->
                        BookWithStatus(book = book, isIssued = book.book_id in issuedIds)
                    }
                    adapter.updateBooksWithStatus(booksWithStatus)
                }
            } else {
                database.bookDao().getBooksBySection(currentSection!!).collect { books ->
                    val issuedIds = issuedBookIdsFlow.value
                    val booksWithStatus = books.map { book ->
                        BookWithStatus(book = book, isIssued = book.book_id in issuedIds)
                    }
                    adapter.updateBooksWithStatus(booksWithStatus)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _adapter = null
    }
}