package com.example.prakt7


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [Book::class, Author::class, Reader::class, Loan::class],
    version = 1,
    exportSchema = false
)
abstract class LibraryDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun authorDao(): AuthorDao  // ✅ Добавили
    abstract fun readerDao(): ReaderDao
    abstract fun loanDao(): LoanDao

    companion object {
        @Volatile
        private var INSTANCE: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()  // ✅ Для отладки: удаляет БД при изменении схемы
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
