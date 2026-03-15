package com.example.prakt7


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddReaderDialogFragment : DialogFragment() {

    private lateinit var database: LibraryDatabase
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRole: Spinner

    private val roles = listOf("student", "teacher", "librarian")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        database = LibraryDatabase.getDatabase(requireContext())

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_reader, null)

        etFullName = view.findViewById(R.id.etReaderFullName)
        etEmail = view.findViewById(R.id.etReaderEmail)
        etPassword = view.findViewById(R.id.etReaderPassword)
        spinnerRole = view.findViewById(R.id.spinnerRole)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        return AlertDialog.Builder(requireContext())
            .setTitle("👥 Добавить читателя")
            .setView(view)
            .setPositiveButton("Добавить") { _, _ ->
                addReader()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private fun addReader() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = spinnerRole.selectedItem.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Ошибка")
                .setMessage("Все поля обязательны для заполнения!")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        if (password.length < 6) {
            AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Ошибка")
                .setMessage("Пароль должен быть не менее 6 символов!")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                database.readerDao().insert(Reader(
                    fullName = fullName,
                    email = email,
                    password = password,
                    role = role,
                    isBlocked = false
                ))
            }

            AlertDialog.Builder(requireContext())
                .setTitle("✅ Успешно")
                .setMessage("Читатель добавлен!")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}