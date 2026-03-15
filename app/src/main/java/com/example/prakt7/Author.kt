package com.example.prakt7

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "authors")
data class Author(
    @PrimaryKey(autoGenerate = true)
    val author_id: Int = 0,

    val name: String,           // ФИО автора
    val biography: String? = null, // Биография (необязательно)
    val birthYear: Int? = null     // Год рождения
)