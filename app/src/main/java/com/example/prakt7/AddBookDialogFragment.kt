package com.example.prakt7


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddBookDialogFragment : DialogFragment() {

    private lateinit var database: LibraryDatabase
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etCoverUrl: EditText
    private lateinit var spinnerSection: Spinner
    private lateinit var switchElectronic: Switch
    private lateinit var btnSearchCover: Button
    private lateinit var ivCoverPreview: ImageView

    private val sections = listOf("Fiction", "Classic", "IT", "Science", "History", "Fantasy", "Other")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        database = LibraryDatabase.getDatabase(requireContext())

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_book, null)

        etTitle = view.findViewById(R.id.etBookTitle)
        etAuthor = view.findViewById(R.id.etBookAuthor)
        etCoverUrl = view.findViewById(R.id.etCoverUrl)
        spinnerSection = view.findViewById(R.id.spinnerSection)
        switchElectronic = view.findViewById(R.id.switchElectronic)
        btnSearchCover = view.findViewById(R.id.btnSearchCover)
        ivCoverPreview = view.findViewById(R.id.ivCoverPreview)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sections)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSection.adapter = adapter

        // ✅ КНОПКА ПОИСКА ОБЛОЖКИ
        btnSearchCover.setOnClickListener {
            searchCover()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("📚 Добавить книгу")
            .setView(view)
            .setPositiveButton("Добавить") { _, _ ->
                addBook()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    // ✅ ПОИСК ОБЛОЖКИ ЧЕРЕЗ GOOGLE BOOKS API
    private fun searchCover() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название книги!", Toast.LENGTH_SHORT).show()
            return
        }

        // Показываем индикатор загрузки
        btnSearchCover.isEnabled = false
        btnSearchCover.text = "⏳"

        lifecycleScope.launch {
            try {
                // Ищем обложку
                val coverUrl = if (author.isNotEmpty()) {
                    CoverSearchHelper.searchCoverByTitleAndAuthor(title, author)
                } else {
                    CoverSearchHelper.searchCoverByTitle(title)
                }

                if (coverUrl != null) {
                    // Нашли обложку!
                    etCoverUrl.setText(coverUrl)

                    // Показываем превью
                    ivCoverPreview.visibility = View.VISIBLE
                    Picasso.get()
                        .load(coverUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivCoverPreview)

                    Toast.makeText(requireContext(), "✅ Обложка найдена!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "⚠️ Обложка не найдена", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSearchCover.isEnabled = true
                btnSearchCover.text = "🔍"
            }
        }
    }

    private fun addBook() {
        val title = etTitle.text.toString().trim()
        val author = etAuthor.text.toString().trim()
        val coverUrl = etCoverUrl.text.toString().trim()
        val section = spinnerSection.selectedItem.toString()
        val isElectronic = switchElectronic.isChecked

        if (title.isEmpty() || author.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Ошибка")
                .setMessage("Название и автор обязательны!")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val authorDao = database.authorDao()
                val bookDao = database.bookDao()

                var authorId = authorDao.getAuthorIdByName(author)
                if (authorId == null) {
                    authorId = authorDao.insert(
                        Author(
                            name = author,
                            birthYear = null,
                            biography = null
                        )
                    ).toInt()
                }

                bookDao.insert(Book(
                    title = title,
                    author_id = authorId,
                    authorName = author,
                    section = section,
                    coverImageUrl = if (coverUrl.isNotEmpty()) coverUrl else null,
                    isElectronic = isElectronic,
                    year = null
                ))
            }

            AlertDialog.Builder(requireContext())
                .setTitle("✅ Успешно")
                .setMessage("Книга добавлена!")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}