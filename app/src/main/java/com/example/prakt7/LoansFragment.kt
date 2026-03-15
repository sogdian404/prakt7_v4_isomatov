package com.example.prakt7


import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class LoansFragment : Fragment(R.layout.fragment_loans) {

    private lateinit var database: LibraryDatabase
    private lateinit var adapter: LoanAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = LibraryDatabase.getDatabase(requireContext())
        recyclerView = view.findViewById(R.id.recyclerViewLoans)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = LoanAdapter(
            emptyList(),
            onReturnClick = { loan ->
                ReturnBookDialogFragment(loan) {
                    // Обновляем список после возврата
                }.show(childFragmentManager, "ReturnBook")
            },
            onItemClick = { loan ->
                // Показать детали
                AlertDialog.Builder(requireContext())
                    .setTitle("📖 Информация о выдаче")
                    .setMessage("""
                        Книга ID: ${loan.book_id}
                        Читатель ID: ${loan.reader_id}
                        Выдана: ${android.text.format.DateFormat.format("dd.MM.yyyy", loan.issueDate)}
                        Вернуть до: ${android.text.format.DateFormat.format("dd.MM.yyyy", loan.dueDate)}
                        Статус: ${loan.getStatus()}
                        Штраф: ${loan.fineAmount} руб.
                    """.trimIndent())
                    .setPositiveButton("OK", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            database.loanDao().getActiveLoans().collect { loans ->
                adapter.updateLoans(loans)
            }
        }
    }
}