package com.example.prakt7

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IssueBookDialogFragment(
    private val book: Book,
    private val onUpdate: () -> Unit
) : DialogFragment() {

    private lateinit var database: LibraryDatabase
    private var selectedReaderIndex = -1
    private var readersList: List<Pair<String, Int>> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!PermissionManager.isLibrarian(requireContext())) {
            AlertDialog.Builder(requireContext())
                .setTitle("Доступ запрещён")
                .setMessage("Только библиотекарь может выдавать книги!")
                .setPositiveButton("OK") { _, _ -> dismiss() }
                .create()
                .show()
            return AlertDialog.Builder(requireContext()).create() // Пустой диалог
        }
        database = LibraryDatabase.getDatabase(requireContext())

        // Показываем диалог загрузки
        val progressDialog = AlertDialog.Builder(requireContext())
            .setTitle("📚 Выдать книгу: ${book.title}")
            .setMessage("Загрузка списка читателей...")
            .setCancelable(false)
            .create()

        // Загружаем читателей
        lifecycleScope.launch {
            try {
                val allReaders = withContext(Dispatchers.IO) {
                    database.readerDao().getAllReaders().first()
                }

                readersList = allReaders.mapNotNull { reader ->
                    // Фильтруем заблокированных читателей
                    if (!reader.isBlocked) {
                        Pair("${reader.fullName} (${reader.email})", reader.reader_id)
                    } else {
                        null
                    }
                }

                // Закрываем диалог загрузки
                progressDialog.dismiss()

                if (readersList.isEmpty()) {
                    AlertDialog.Builder(requireContext())
                        .setTitle("❌ Ошибка")
                        .setMessage("Нет доступных читателей в базе!")
                        .setPositiveButton("OK") { _, _ ->
                            dismiss()
                        }
                        .show()
                    return@launch
                }

                // Показываем диалог выбора читателя
                showReaderSelectionDialog()

            } catch (e: Exception) {
                progressDialog.dismiss()
                AlertDialog.Builder(requireContext())
                    .setTitle("❌ Ошибка")
                    .setMessage("Ошибка: ${e.message}")
                    .setPositiveButton("OK") { _, _ ->
                        dismiss()
                    }
                    .show()
            }
        }

        return progressDialog
    }

    private fun showReaderSelectionDialog() {
        // Проверяем, что фрагмент ещё прикреплен
        if (!isAdded) return

        val readerNames = readersList.map { it.first }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("📚 Выдать книгу: ${book.title}")
            .setSingleChoiceItems(readerNames, -1) { dialog: DialogInterface, which: Int ->
                selectedReaderIndex = which
            }
            .setPositiveButton("Выдать") { dialog: DialogInterface, _ ->
                if (selectedReaderIndex >= 0) {
                    val readerId = readersList[selectedReaderIndex].second
                    lifecycleScope.launch {
                        issueBook(readerId)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Выберите читателя!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Отмена") { dialog: DialogInterface, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private suspend fun issueBook(readerId: Int) {
        try {
            withContext(Dispatchers.IO) {
                // Проверяем, не взята ли уже книга
                val existingLoan = database.loanDao().getActiveLoanByBook(book.book_id)
                if (existingLoan != null) {
                    throw Exception("Книга уже выдана!")
                }

                // Проверяем читателя
                val reader = database.readerDao().getReaderById(readerId)
                if (reader == null) {
                    throw Exception("Читатель не найден!")
                }
                if (reader.isBlocked) {
                    throw Exception("Читатель заблокирован!")
                }

                // Проверяем, сколько книг уже взял читатель (опционально)
                val activeLoans = database.loanDao().getActiveLoansCount(readerId)
                if (activeLoans >= 5) { // Ограничение: максимум 5 книг
                    throw Exception("Читатель уже взял максимум книг (5)!")
                }

                // Создаём выдачу (срок 14 дней)
                val issueDate = System.currentTimeMillis()
                val dueDate = issueDate + (14 * 24 * 60 * 60 * 1000L)

                database.loanDao().insert(
                    Loan(
                        book_id = book.book_id,
                        reader_id = readerId,
                        issueDate = issueDate,
                        dueDate = dueDate,
                        returnDate = null,
                        fineAmount = 0.0
                    )
                )
            }

            // Показываем успех БЕЗОПАСНО
            if (isAdded && !isRemoving && !isDetached) {
                AlertDialog.Builder(requireContext())
                    .setTitle("✅ Успешно")
                    .setMessage("Книга \"${book.title}\" выдана!\nСрок возврата: 14 дней")
                    .setPositiveButton("OK") { _, _ ->
                        try {
                            onUpdate()
                        } catch (e: Exception) {
                            // Игнорируем ошибки onUpdate
                        }
                        dismiss()
                    }
                    .setOnCancelListener {
                        dismiss()
                    }
                    .show()
            }

        } catch (e: Exception) {
            // Показываем ошибку БЕЗОПАСНО
            if (isAdded && !isRemoving && !isDetached) {
                AlertDialog.Builder(requireContext())
                    .setTitle("❌ Ошибка")
                    .setMessage(e.message ?: "Неизвестная ошибка")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}