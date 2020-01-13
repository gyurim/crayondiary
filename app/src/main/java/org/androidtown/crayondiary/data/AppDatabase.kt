package org.androidtown.crayondiary.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.androidtown.crayondiary.util.MainApplication

@Database(entities = [Diary::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        val instance = Room.databaseBuilder(
            MainApplication.context,
            AppDatabase::class.java, "crayondiary.db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}