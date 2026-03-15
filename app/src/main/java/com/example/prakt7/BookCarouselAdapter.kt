package com.example.prakt7

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

data class BookWithStatus(
    val book: Book,
    val isIssued: Boolean
)

class BookCarouselAdapter(
    private var booksWithStatus: List<BookWithStatus> = emptyList(),
    private val onItemClick: (Book) -> Unit,
    private val onItemLongClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<BookCarouselAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCover: ImageView = itemView.findViewById(R.id.ivBookCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        private val tvAuthor: TextView = itemView.findViewById(R.id.tvBookAuthor)
        private val tvType: TextView = itemView.findViewById(R.id.tvBookType)

        fun bind(bookWithStatus: BookWithStatus) {
            val book = bookWithStatus.book
            val isIssued = bookWithStatus.isIssued

            tvTitle.text = book.title
            tvAuthor.text = book.authorName

            tvType.text = if (book.isElectronic) "💻 Электронная" else "📚 Печатная"

            // ✅ ДОБАВЛЯЕМ СТАТУС "ВЫДАНА"
            if (isIssued) {
                tvType.text = "🚫 ВЫДАНА"
                tvType.setBackgroundColor(
                    itemView.context.getColor(android.R.color.holo_red_dark)
                )
                // Делаем карточку полупрозрачной
                itemView.alpha = 0.6f
            } else {
                tvType.setBackgroundColor(
                    if (book.isElectronic)
                        itemView.context.getColor(android.R.color.holo_purple)
                    else
                        itemView.context.getColor(android.R.color.holo_blue_light)
                )
                itemView.alpha = 1.0f
            }

            Picasso.get()
                .load(book.coverImageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivCover)

            // Клик
            itemView.setOnClickListener {
                if (!isIssued) {
                    onItemClick(book)
                } else {
                    // Показываем что книга уже выдана
                    AlertDialog.Builder(itemView.context)
                        .setTitle("📚 ${book.title}")
                        .setMessage("Эта книга уже выдана другому читателю!\n\nАвтор: ${book.authorName}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }

            // Долгий клик (только для невыданных книг)
            itemView.setOnLongClickListener {
                if (!isIssued) {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("🗑️ Удалить книгу?")
                        .setMessage("Вы уверены, что хотите удалить «${book.title}»?")
                        .setPositiveButton("Удалить") { _, _ ->
                            onItemLongClick(book)
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                    true
                } else {
                    false // Нельзя удалить выданную книгу
                }
            }
        }
    }

    fun updateBooksWithStatus(newBooks: List<BookWithStatus>) {
        booksWithStatus = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book_card, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(booksWithStatus[position])
    }

    override fun getItemCount(): Int = booksWithStatus.size
}