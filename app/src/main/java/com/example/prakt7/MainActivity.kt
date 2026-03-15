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