package com.example.prakt7


import android.content.Context

object PermissionManager {

    // Роли
    const val ROLE_STUDENT = "student"
    const val ROLE_TEACHER = "teacher"
    const val ROLE_LIBRARIAN = "librarian"

    // Получаем текущую роль пользователя
    fun getUserRole(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userRole", ROLE_STUDENT) ?: ROLE_STUDENT
    }

    // Проверка: является ли пользователь библиотекарем
    fun isLibrarian(context: Context): Boolean {
        return getUserRole(context) == ROLE_LIBRARIAN
    }

    // Проверка: является ли пользователем преподавателем
    fun isTeacher(context: Context): Boolean {
        return getUserRole(context) == ROLE_TEACHER
    }

    // Проверка: является ли пользователем студентом
    fun isStudent(context: Context): Boolean {
        return getUserRole(context) == ROLE_STUDENT
    }

    // Получаем понятное название роли
    fun getRoleName(context: Context): String {
        return when (getUserRole(context)) {
            ROLE_LIBRARIAN -> "📚 Библиотекарь"
            ROLE_TEACHER -> "👨‍🏫 Преподаватель"
            ROLE_STUDENT -> "🎓 Студент"
            else -> "👤 Пользователь"
        }
    }
}