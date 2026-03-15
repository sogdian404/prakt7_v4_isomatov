package com.example.prakt7


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Author::class,
            parentColumns = ["author_id"],
            childColumns = ["author_id"],
            onDelete = ForeignKey.CASCADE // Если удалили автора — удаляем его книги
        )
    ],
    indices = [Index("author_id"), Index("section")] // Индексы для быстрого поиска
)
data class Book(
    @PrimaryKey(autoGenerate = true)  // ✅ Автоинкремент первичного ключа
    val book_id: Int = 0,             // Номенклатурный номер (заданию: +1 при добавлении)

    val title: String,                // Название книги
    val author_id: Int,               // Ссылка на автора (внешний ключ)
    val authorName: String,
    val section: String,              // Раздел библиотеки
    val coverImageUrl: String?,       // Ссылка на обложку (для API)
    val isElectronic: Boolean,        // Электронная или печатная

    // Дополнительные поля для удобства
    val description: String? = null,  // Описание
    val year: Int? = null             // Год издания
)