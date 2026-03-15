package com.example.prakt7

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DebugHelper {

    private const val TAG = "DEBUG_HELPER"

    /**
     * Создаёт тестовые данные для демонстрации штрафов и блокировок
     */
    suspend fun createTestScenarios(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = LibraryDatabase.getDatabase(context)
                val bookDao = db.bookDao()
                val authorDao = db.authorDao()
                val readerDao = db.readerDao()
                val loanDao = db.loanDao()

                Log.d(TAG, "🧪 Создаём тестовые сценарии...")

                // === СЦЕНАРИЙ 1: Читатель с штрафом (просрочка 30 дней) ===

                // 1. Создаём автора и книгу (если нет)
                var author = authorDao.getAuthorIdByName("Ф. Достоевский")
                if (author == null) {
                    author = authorDao.insert(Author(name = "Ф. Достоевский")).toInt()
                }

                var book = bookDao.getBookByTitle("Преступление и наказание")
                if (book == null) {
                    bookDao.insert(Book(
                        title = "Преступление и наказание",
                        author_id = author,
                        authorName = "Ф. Достоевский",
                        section = "Classic",
                        coverImageUrl = "https://covers.openlibrary.org/b/id/8739103-L.jpg",
                        isElectronic = false
                    ))
                    book = bookDao.getBookByTitle("Преступление и наказание")
                }

                // 2. Создаём читателя "Иванов" (будет со штрафом)
                var readerFine = readerDao.getReaderByEmail("ivanov@test.ru")
                if (readerFine == null) {
                    readerFine = Reader(
                        fullName = "Иванов Иван (штраф)",
                        email = "ivanov@test.ru",
                        password = "123456",
                        role = "student",
                        isBlocked = false
                    )
                    readerDao.insert(readerFine)
                    readerFine = readerDao.getReaderByEmail("ivanov@test.ru")
                }

                // 3. Создаём выдачу с просрочкой 30 дней
                val now = System.currentTimeMillis()
                val issueDateFine = now - (44 * 24 * 60 * 60 * 1000L) // Выдана 44 дня назад
                val dueDateFine = now - (30 * 24 * 60 * 60 * 1000L)   // Срок был 30 дней назад

                // Проверяем, нет ли уже такой выдачи
                val existingLoanFine = loanDao.getActiveLoanByBook(book!!.book_id)
                if (existingLoanFine == null) {
                    loanDao.insert(Loan(
                        book_id = book.book_id,
                        reader_id = readerFine!!.reader_id,
                        issueDate = issueDateFine,
                        dueDate = dueDateFine,
                        returnDate = null,  // Не возвращена
                        fineAmount = 0.0
                    ))
                    Log.d(TAG, "✅ Создан читатель со штрафом: Иванов Иван (30 дней просрочки)")
                }

                // === СЦЕНАРИЙ 2: Заблокированный читатель (просрочка > 60 дней) ===

                // 1. Книга для второго сценария
                var author2 = authorDao.getAuthorIdByName("Л. Толстой")
                if (author2 == null) {
                    author2 = authorDao.insert(Author(name = "Л. Толстой")).toInt()
                }

                var book2 = bookDao.getBookByTitle("Война и мир")
                if (book2 == null) {
                    bookDao.insert(Book(
                        title = "Война и мир",
                        author_id = author2,
                        authorName = "Л. Толстой",
                        section = "Classic",
                        coverImageUrl = "https://covers.openlibrary.org/b/id/10898847-L.jpg",
                        isElectronic = false
                    ))
                    book2 = bookDao.getBookByTitle("Война и мир")
                }

                // 2. Создаём читателя "Петров" (будет заблокирован)
                var readerBlocked = readerDao.getReaderByEmail("petrov@test.ru")
                if (readerBlocked == null) {
                    readerBlocked = Reader(
                        fullName = "Петров Петр (заблокирован)",
                        email = "petrov@test.ru",
                        password = "123456",
                        role = "student",
                        isBlocked = false  // Заблокируем после создания выдачи
                    )
                    readerDao.insert(readerBlocked)
                    readerBlocked = readerDao.getReaderByEmail("petrov@test.ru")
                }

                // 3. Создаём выдачу с просрочкой 90 дней
                val issueDateBlocked = now - (104 * 24 * 60 * 60 * 1000L) // Выдана 104 дня назад
                val dueDateBlocked = now - (90 * 24 * 60 * 60 * 1000L)    // Срок был 90 дней назад

                val existingLoanBlocked = loanDao.getActiveLoanByBook(book2!!.book_id)
                if (existingLoanBlocked == null) {
                    loanDao.insert(Loan(
                        book_id = book2.book_id,
                        reader_id = readerBlocked!!.reader_id,
                        issueDate = issueDateBlocked,
                        dueDate = dueDateBlocked,
                        returnDate = null,  // Не возвращена
                        fineAmount = 0.0
                    ))

                    // ✅ Блокируем читателя (так как просрочка > 60 дней)
                    readerDao.update(readerBlocked.copy(isBlocked = true))

                    Log.d(TAG, "✅ Создан заблокированный читатель: Петров Петр (90 дней просрочки)")
                }

                Log.d(TAG, "🎉 Тестовые сценарии созданы успешно!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка при создании тестовых данных: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}