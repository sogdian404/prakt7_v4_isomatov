package com.example.prakt7

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var spinnerRole: Spinner

    private lateinit var sharedPreferences: SharedPreferences

    // Роли пользователей
    private val roles = listOf("student", "teacher", "librarian")
    private val roleNames = listOf("🎓 Студент", "👨‍🏫 Преподаватель", "📚 Библиотекарь")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        spinnerRole = findViewById(R.id.spinnerRole)

        sharedPreferences = getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)

        // Настраиваем Spinner с ролями
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roleNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        // Проверяем, был ли пользователь уже авторизован
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            goToMainActivity()
            return
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val selectedRole = roles[spinnerRole.selectedItemPosition]

            if (validateData(email, password)) {
                saveLoginState(email, selectedRole)
                goToMainActivity()
            }
        }
    }

    private fun validateData(email: String, password: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный Email")
            return false
        }
        if (password.isEmpty() || password.length < 6) {
            showError("Пароль должен быть не менее 6 символов")
            return false
        }
        return true
    }

    private fun saveLoginState(email: String, role: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("userEmail", email)
        editor.putString("userRole", role)  // ✅ Сохраняем роль
        editor.apply()
    }

    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = TextView.VISIBLE
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}