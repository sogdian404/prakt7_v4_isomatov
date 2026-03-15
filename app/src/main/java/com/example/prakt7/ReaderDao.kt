package com.example.prakt7


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderDao {

    @Query("SELECT * FROM readers ORDER BY fullName")
    fun getAllReaders(): Flow<List<Reader>>

    @Query("SELECT * FROM readers WHERE reader_id = :readerId")
    suspend fun getReaderById(readerId: Int): Reader?

    @Query("SELECT * FROM readers WHERE email = :email LIMIT 1")
    suspend fun getReaderByEmail(email: String): Reader?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reader: Reader): Long

    @Update
    suspend fun update(reader: Reader)

    @Delete
    suspend fun delete(reader: Reader)

    @Query("SELECT COUNT(*) FROM readers")
    suspend fun getReadersCount(): Int
}