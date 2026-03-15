package com.example.prakt7

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {

    @Query("SELECT * FROM authors ORDER BY name")
    fun getAllAuthors(): Flow<List<Author>>

    @Query("SELECT * FROM authors WHERE author_id = :authorId")
    suspend fun getAuthorById(authorId: Int): Author?

    @Query("SELECT author_id FROM authors WHERE name = :name LIMIT 1")
    suspend fun getAuthorIdByName(name: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(author: Author): Long

    @Update
    suspend fun update(author: Author)

    @Delete
    suspend fun delete(author: Author)
}