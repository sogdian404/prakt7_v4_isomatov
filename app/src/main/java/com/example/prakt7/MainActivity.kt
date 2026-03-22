package com.example.prakt7

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Проверяем авторизацию
        if (!checkAuth()) {
            redirectToLogin()
            return
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // ✅ ОГРАНИЧИВАЕМ МЕНЮ ПО РОЛИ
        setupNavigationByRole()
        // Обработчик переключения вкладок
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_books -> {
                    loadFragment(BooksCarouselFragment())
                    true
                }
                R.id.nav_readers -> {
                    loadFragment(ReadersFragment())
                    true
                }
                R.id.nav_loans -> {  // ✅ Новая вкладка
                    loadFragment(LoansFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Загружаем первый фрагмент по умолчанию
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_books
        }
    }
    private fun setupNavigationByRole() {
        val menu = bottomNavigationView.menu

        if (PermissionManager.isLibrarian(this)) {
            // 📚 Библиотекарь: видит ВСЕ вкладки (4)
            menu.findItem(R.id.nav_books)?.isVisible = true
            menu.findItem(R.id.nav_readers)?.isVisible = true
            menu.findItem(R.id.nav_loans)?.isVisible = true
            menu.findItem(R.id.nav_profile)?.isVisible = true

            // Меняем заголовок
            supportActionBar?.title = "📚 Библиотека — Библиотекарь"

        } else if (PermissionManager.isTeacher(this)) {
            // 👨‍ Преподаватель: видит Книги, Выдачи, Профиль (3)
            menu.findItem(R.id.nav_books)?.isVisible = true
            menu.findItem(R.id.nav_readers)?.isVisible = false  // Скрываем читателей
            menu.findItem(R.id.nav_loans)?.isVisible = true     // Может смотреть выдачи
            menu.findItem(R.id.nav_profile)?.isVisible = true

            supportActionBar?.title = "📚 Библиотека — Преподаватель"

        } else {
            // 🎓 Студент: видит только Книги и Профиль (2)
            menu.findItem(R.id.nav_books)?.isVisible = true
            menu.findItem(R.id.nav_readers)?.isVisible = false  // Скрываем читателей
            menu.findItem(R.id.nav_loans)?.isVisible = true    // Скрываем выдачи
            menu.findItem(R.id.nav_profile)?.isVisible = true

            supportActionBar?.title = "📚 Библиотека — Студент"
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun checkAuth(): Boolean {
        val sharedPreferences = getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    private fun redirectToLogin() {
        val intent = android.content.Intent(this, LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}