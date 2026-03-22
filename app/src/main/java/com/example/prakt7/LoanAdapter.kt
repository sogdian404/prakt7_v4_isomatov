package com.example.prakt7


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LoanAdapter(
    private var loans: List<Loan> = emptyList(),
    private val onReturnClick: (Loan) -> Unit,
    private val onItemClick: (Loan) -> Unit,
    private val isLibrarian: Boolean
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    inner class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBookId: TextView = itemView.findViewById(R.id.tvLoanBookId)
        private val tvReaderId: TextView = itemView.findViewById(R.id.tvLoanReaderId)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvLoanDueDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvLoanStatus)
        private val btnReturn: Button = itemView.findViewById(R.id.btnReturnBook)

        fun bind(loan: Loan) {
            tvBookId.text = "📚 Книга ID: ${loan.book_id}"
            tvReaderId.text = "👤 Читатель ID: ${loan.reader_id}"
            tvDueDate.text = "📅 Вернуть до: ${android.text.format.DateFormat.format("dd.MM.yyyy", loan.dueDate)}"
            tvStatus.text = loan.getStatus()

            // Цвет статуса
            tvStatus.setTextColor(
                when {
                    loan.returnDate != null -> itemView.context.getColor(android.R.color.holo_green_dark)
                    loan.getOverdueDays() > 60 -> itemView.context.getColor(android.R.color.holo_red_dark)
                    loan.getOverdueDays() > 0 -> itemView.context.getColor(android.R.color.holo_orange_dark)
                    else -> itemView.context.getColor(android.R.color.holo_blue_dark)
                }
            )

            itemView.setOnClickListener { onItemClick(loan) }

            if (isLibrarian) {
                btnReturn.visibility = View.VISIBLE
                btnReturn.setOnClickListener { onReturnClick(loan) }
            } else {
                btnReturn.visibility = View.GONE
            }
        }
    }

    fun updateLoans(newLoans: List<Loan>) {
        loans = newLoans
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_card, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        holder.bind(loans[position])
    }

    override fun getItemCount(): Int = loans.size
}