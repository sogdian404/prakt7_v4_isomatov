package com.example.prakt7

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    // Получить все книги
    @Query("SELECT * FROM books ORDER BY title")
    fun getAllBooks(): Flow<List<Book>>



    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int
    // Получить книги по разделу (группировка по разделам — требование задания)
    @Query("SELECT * FROM books WHERE section = :sectionName ORDER BY title")
    fun getBooksBySection(sectionName: String): Flow<List<Book>>

    // Получить книгу по ID
    @Query("SELECT * FROM books WHERE book_id = :bookId")
    suspend fun getBookById(bookId: Int): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book): Long  // Возвращает новый ID (номенклатурный номер)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM books WHERE title LIKE :query OR author_id IN (SELECT author_id FROM authors WHERE name LIKE :query)")
    fun searchBooks(query: String): Flow<List<Book>>
    @Query("SELECT * FROM books WHERE title = :title LIMIT 1")
    suspend fun getBookByTitle(title: String): Book?
    // Получить все уникальные разделы (для фильтра)
    @Query("SELECT DISTINCT section FROM books ORDER BY section")
    suspend fun getAllSections(): List<String>
}