package com.example.prakt7

import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.Response

interface BooksApi {
    @GET("volumes")
    suspend fun searchBooks(@Query("q") query: String): Response<BookSearchResponse>
}

data class BookSearchResponse(
    val items: List<BookItem>?
)

data class BookItem(
    val id: String,
    val volumeInfo: VolumeInfo
)

data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val imageLinks: ImageLinks?
)

data class ImageLinks(
    val thumbnail: String?
)