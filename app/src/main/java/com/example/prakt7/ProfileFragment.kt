package com.example.prakt7

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("LibraryPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("userEmail", "Не указан")

        view.findViewById<TextView>(R.id.tvProfileEmail)?.text = "📧 $email"
        view.findViewById<TextView>(R.id.tvProfileRole)?.text = "🔑 ${PermissionManager.getRoleName(requireContext())}"

        // ✅ Кнопка выхода
        view.findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }

        // ✅ Кнопка для тестовых данных (показываем только библиотекарям в отладке)
        val btnDebug = view.findViewById<Button>(R.id.btnDebugTest)
        if (PermissionManager.isLibrarian(requireContext())) {
            btnDebug?.visibility = View.VISIBLE
            btnDebug?.setOnClickListener {
                createDebugTestData()
            }
        }
    }

    // ✅ Создаёт тестовые данные для демонстрации
    private fun createDebugTestData() {
        Toast.makeText(requireContext(), "⏳ Создаём тестовые данные...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.Main).launch {
            DebugHelper.createTestScenarios(requireContext())

            Toast.makeText(
                requireContext(),
                "✅ Готово!\n• Иванов Иван: штраф ~300 руб.\n• Петров Петр: заблокирован",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}