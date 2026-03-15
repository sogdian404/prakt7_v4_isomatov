package com.example.prakt7


import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(entity = Book::class, parentColumns = ["book_id"], childColumns = ["book_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Reader::class, parentColumns = ["reader_id"], childColumns = ["reader_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val loan_id: Int = 0,

    val book_id: Int,
    val reader_id: Int,
    val issueDate: Long,        // Дата выдачи
    val dueDate: Long,          // Дата возврата (дедлайн)
    val returnDate: Long? = null, // Фактическая дата возврата
    val fineAmount: Double = 0.0  // Накопленный штраф
) {
    // === ЛОГИКА ШТРАФОВ (требование задания) ===

    // Просрочка в днях
    fun getOverdueDays(): Int {
        val now = System.currentTimeMillis()
        val returnOrNow = returnDate ?: now
        val diffMillis = returnOrNow - dueDate
        return if (diffMillis > 0) (diffMillis / (1000 * 60 * 60 * 24)).toInt() else 0
    }

    // Есть ли просрочка > 60 дней (запрет на выдачу)
    fun isBlockedForOverdue(): Boolean = getOverdueDays() > 60

    // Рассчитать пеню (10 рублей в день просрочки)
    fun calculateFine(): Double {
        val overdue = getOverdueDays()
        return if (overdue > 0) overdue * 10.0 else 0.0
    }

    // Статус выдачи
    fun getStatus(): String {
        return when {
            returnDate != null -> "✅ Возвращена"
            getOverdueDays() > 60 -> "🚫 Блокировка (>60 дней)"
            getOverdueDays() > 0 -> "⚠️ Просрочена (${getOverdueDays()} дн.)"
            else -> "📖 Активна"
        }
    }
}