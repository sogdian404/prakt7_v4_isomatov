package com.example.prakt7

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CoverSearchHelper {
    // ✅ Поиск обложки через Google Books API
    suspend fun searchCoverByTitle(title: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.booksApi.searchBooks(title)

                if (response.isSuccessful) {
                    val firstItem = response.body()?.items?.firstOrNull()
                    var coverUrl = firstItem?.volumeInfo?.imageLinks?.thumbnail

                    // ✅ Заменяем http на https (как у одногруппника)
                    coverUrl = coverUrl?.replace("http://", "https://")

                    coverUrl
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // ✅ Поиск обложки по названию + автору (более точный)
    suspend fun searchCoverByTitleAndAuthor(title: String, author: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Ищем по "название + автор"
                val query = "$title $author"
                val response = ApiClient.booksApi.searchBooks(query)

                if (response.isSuccessful) {
                    val firstItem = response.body()?.items?.firstOrNull()
                    var coverUrl = firstItem?.volumeInfo?.imageLinks?.thumbnail

                    coverUrl = coverUrl?.replace("http://", "https://")

                    coverUrl
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}