package com.example.prakt7

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "readers")
data class Reader(
    @PrimaryKey(autoGenerate = true)
    val reader_id: Int = 0,

    val fullName: String,       // ФИО читателя
    val email: String,          // Email (для авторизации)
    val password: String,       // Пароль (в реальном приложении — хешированный!)
    val role: String,           // "student", "teacher", "librarian"
    val isBlocked: Boolean = false, // Заблокирован ли за просрочку
    val registrationDate: Long = System.currentTimeMillis() // Дата регистрации
)