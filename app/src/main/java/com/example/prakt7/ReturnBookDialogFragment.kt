package com.example.prakt7


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReturnBookDialogFragment(
    private val loan: Loan,
    private val onUpdate: () -> Unit
) : DialogFragment() {

    private lateinit var database: LibraryDatabase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        database = LibraryDatabase.getDatabase(requireContext())

        val overdueDays = loan.getOverdueDays()
        val fine = loan.calculateFine()

        var message = "Вернуть книгу?"
        if (overdueDays > 0) {
            message += "\n\n⚠️ Просрочка: $overdueDays дн."
            message += "\n💰 Штраф: ${fine} руб."
        }
        if (overdueDays > 60) {
            message += "\n\n🚫 ВНИМАНИЕ: Просрочка более 60 дней!"
            message += "\nЧитатель будет заблокирован."
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("📖 Возврат книги")
            .setMessage(message)
            .setPositiveButton("Вернуть") { _, _ ->
                lifecycleScope.launch {
                    returnBook()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
    }

    private suspend fun returnBook() {
        withContext(Dispatchers.IO) {
            val returnDate = System.currentTimeMillis()
            val fine = loan.calculateFine()

            // Обновляем выдачу
            val updatedLoan = loan.copy(
                returnDate = returnDate,
                fineAmount = fine
            )
            database.loanDao().update(updatedLoan)

            // Если просрочка > 60 дней — блокируем читателя
            if (loan.getOverdueDays() > 60) {
                val reader = database.readerDao().getReaderById(loan.reader_id)
                reader?.let {
                    database.readerDao().update(it.copy(isBlocked = true))
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("✅ Книга возвращена")
            .setMessage("Штраф: ${loan.calculateFine()} руб." )
        .setPositiveButton("OK") { _, _ ->
            onUpdate()
        }
            .show()

        dismiss()
    }
}