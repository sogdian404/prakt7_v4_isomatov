package com.example.prakt7

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    // Получить все активные выдачи (книга ещё не возвращена)
    @Query("SELECT * FROM loans WHERE returnDate IS NULL ORDER BY dueDate")
    fun getActiveLoans(): Flow<List<Loan>>

    // Получить список ID выданных книг
    @Query("SELECT book_id FROM loans WHERE returnDate IS NULL")
    suspend fun getIssuedBookIds(): List<Int>

    // Получить все выдачи читателя
    @Query("SELECT * FROM loans WHERE reader_id = :readerId ORDER BY issueDate DESC")
    fun getLoansByReader(readerId: Int): Flow<List<Loan>>

    // Получить выдачу по ID книги (проверить, не взята ли книга)
    @Query("SELECT * FROM loans WHERE book_id = :bookId AND returnDate IS NULL LIMIT 1")
    suspend fun getActiveLoanByBook(bookId: Int): Loan?

    // Получить количество активных книг у читателя
    @Query("SELECT COUNT(*) FROM loans WHERE reader_id = :readerId AND returnDate IS NULL")
    suspend fun getActiveLoansCount(readerId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: Loan): Long

    @Update
    suspend fun update(loan: Loan)

    @Delete
    suspend fun delete(loan: Loan)

    // Получить все просроченные выдачи
    @Query("SELECT * FROM loans WHERE returnDate IS NULL AND dueDate < :currentTime")
    suspend fun getOverdueLoans(currentTime: Long = System.currentTimeMillis()): List<Loan>
}