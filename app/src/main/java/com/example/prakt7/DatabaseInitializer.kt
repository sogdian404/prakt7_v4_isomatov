package com.example.prakt7


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseInitializer {

    private const val TAG = "DB_INIT"

    suspend fun initializeIfNeeded(database: LibraryDatabase) {
        val bookDao = database.bookDao()
        val authorDao = database.authorDao()  // ✅ Получаем DAO для авторов

        Log.d(TAG, "🔍 Проверяем, заполнена ли база...")

        withContext(Dispatchers.IO) {
            try {
                val bookCount = bookDao.getBooksCount()
                Log.d(TAG, "📊 В базе сейчас книг: $bookCount")

                if (bookCount == 0) {
                    Log.d(TAG, "📭 База пустая, заполняем тестовыми данными...")

                    // ✅ 1. СНАЧАЛА вставляем авторов
                    val author1Id = authorDao.insert(Author(
                        name = "Джордж Оруэлл",
                        birthYear = 1903,
                        biography = "Английский писатель"
                    )).toInt()
                    Log.d(TAG, "✅ Добавлен автор: Джордж Оруэлл (ID=$author1Id)")

                    val author2Id = authorDao.insert(Author(
                        name = "М. Булгаков",
                        birthYear = 1891,
                        biography = "Русский писатель"
                    )).toInt()
                    Log.d(TAG, "✅ Добавлен автор: М. Булгаков (ID=$author2Id)")

                    val author3Id = authorDao.insert(Author(
                        name = "Robert Martin",
                        birthYear = 1946,
                        biography = "Американский программист"
                    )).toInt()
                    Log.d(TAG, "✅ Добавлен автор: Robert Martin (ID=$author3Id)")

                    // ✅ 2. ПОТОМ вставляем книги с правильными author_id
                    bookDao.insert(Book(
                        title = "1984",
                        author_id = author1Id,  // ✅ Используем реальный ID из базы
                        authorName = "Джордж Оруэлл",
                        section = "Fiction",
                        coverImageUrl = "https://covers.openlibrary.org/b/id/8225261-L.jpg",
                        isElectronic = false,
                        year = 1949
                    ))
                    Log.d(TAG, "✅ Добавлена книга: 1984")

                    bookDao.insert(Book(
                        title = "Мастер и Маргарита",
                        author_id = author2Id,
                        authorName = "М. Булгаков",
                        section = "Classic",
                        coverImageUrl = "https://covers.openlibrary.org/b/id/10209943-L.jpg",
                        isElectronic = false,
                        year = 1967
                    ))
                    Log.d(TAG, "✅ Добавлена книга: Мастер и Маргарита")

                    bookDao.insert(Book(
                        title = "Clean Code",
                        author_id = author3Id,
                        authorName = "Robert Martin",
                        section = "IT",
                        coverImageUrl = "https://covers.openlibrary.org/b/id/8342947-L.jpg",
                        isElectronic = true,
                        year = 2008
                    ))
                    Log.d(TAG, "✅ Добавлена книга: Clean Code")

                    Log.d(TAG, "🎉 Все данные добавлены успешно!")

                } else {
                    Log.d(TAG, "✅ База уже заполнена")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ ОШИБКА при инициализации: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}