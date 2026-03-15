package com.example.prakt7
import com.google.gson.annotations.SerializedName
// Ответ от OpenLibrary API
data class OpenLibraryResponse(
    val docs: List<Doc>?
)

data class Doc(
    val title: String?,
    val author_name: List<String>?,
    @SerializedName("cover_i")
    val coverId: Int?
)