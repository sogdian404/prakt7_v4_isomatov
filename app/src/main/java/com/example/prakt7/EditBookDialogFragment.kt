package com.example.prakt7

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditBookDialogFragment(
    private val book: Book,
    private val onUpdate: () -> Unit
) : DialogFragment() {

    private lateinit var database: LibraryDatabase
    private lateinit var etTitle: EditText
    private lateinit var etAuthor: EditText
    private lateinit var etCoverUrl: EditText
    private lateinit var spinnerSection: Spinner
    private lateinit var switchElectronic: Switch

    private val sections = listOf("Вымисел", "Классика", "IT", "Наука", "История", "Фантастика", "Другое")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        database = LibraryDatabase.getDatabase(requireContext())

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_book, null)

        etTitle = view.findViewById(R.id.etBookTitle)
        etAuthor = view.findViewById(R.id.etBookAuthor)
        etCoverUrl = view.findViewById(R.id.etCoverUrl)
        spinnerSection = view.findViewById(R.id.spinnerSection)
        switchElectronic = view.findViewById(R.id.switchElectronic)

        // ✅ ЗАПОЛНЯЕМ ПОЛЯ ТЕКУЩИМИ ДАННЫМИ
        etTitle.setText(book.title)
        etAuthor.setText(book.authorName)
        etCoverUrl.setText(book.coverImageUrl ?: "")
        switchElectronic.isChecked = book.isElectronic

        // Выбираем текущий раздел в Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sections)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSection.adapter = adapter

        val sectionIndex = sections.indexOf(book.section)
        if (sectionIndex >= 0) {
            spinnerSection.setSelection(sectionIndex)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("✏️ Редактировать книгу")
            .setView(view)
            .setPositiveButton("Сохранить") { _, _ ->
                saveChanges()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun saveChanges() {
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
            try {
                withContext(Dispatchers.IO) {
                    val authorDao = database.authorDao()
                    val bookDao = database.bookDao()

                    // Ищем или создаём автора
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

                    // Обновляем книгу
                    val updatedBook = book.copy(
                        title = title,
                        author_id = authorId,
                        authorName = author,
                        section = section,
                        coverImageUrl = if (coverUrl.isNotEmpty()) coverUrl else null,
                        isElectronic = isElectronic
                    )

                    bookDao.update(updatedBook)
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("✅ Успешно")
                    .setMessage("Книга обновлена!")
                    .setPositiveButton("OK") { _, _ ->
                        onUpdate()
                    }
                    .show()

            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setTitle("❌ Ошибка")
                    .setMessage(e.message ?: "Не удалось обновить книгу")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}