package com.example.prakt7


import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReaderAdapter(
    private var readers: List<Reader> = emptyList(),
    private val onItemClick: (Reader) -> Unit,
    private val onItemLongClick: (Reader) -> Unit = {}
) : RecyclerView.Adapter<ReaderAdapter.ReaderViewHolder>() {

    inner class ReaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvReaderName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvReaderEmail)
        private val tvRole: TextView = itemView.findViewById(R.id.tvReaderRole)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvReaderStatus)

        fun bind(reader: Reader) {
            tvName.text = reader.fullName
            tvEmail.text = reader.email
            tvRole.text = when (reader.role) {
                "Студент" -> "🎓 Студент"
                "Преподаватель" -> "👨‍🏫 Преподаватель"
                "Библиотекарь" -> "📚 Библиотекарь"
                else -> "👤 Пользователь"
            }

            // Статус: заблокирован или нет
            tvStatus.text = if (reader.isBlocked) "🚫 Заблокирован" else "✅ Активен"
            tvStatus.setTextColor(
                if (reader.isBlocked)
                    itemView.context.getColor(android.R.color.holo_red_dark)
                else
                    itemView.context.getColor(android.R.color.holo_green_dark)
            )

            // Клик — показать информацию
            itemView.setOnClickListener { onItemClick(reader) }

            // Долгий клик — удалить
            itemView.setOnLongClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("🗑️ Удалить читателя?")
                    .setMessage("Вы уверены, что хотите удалить «${reader.fullName}»?")
                    .setPositiveButton("Удалить") { _, _ ->
                        onItemLongClick(reader)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                true
            }
        }
    }

    fun updateReaders(newReaders: List<Reader>) {
        readers = newReaders
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reader_card, parent, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        holder.bind(readers[position])
    }

    override fun getItemCount(): Int = readers.size
}