package org.androidtown.crayondiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary") /*이 query에 해당하는 동작은*/
    fun getAll(): List<Diary> /*이렇게 선언한다.*/

    @Query("SELECT * FROM diary WHERE id = :id")
    fun get(id: Int): Diary

    @Insert(onConflict = REPLACE)
    fun insert(diary: Diary): Long

    @Query("DELETE from diary WHERE id = :id")
    fun delete(id: Int)

    @Update
    fun update(diary: Diary)
}

/*Diary라는 클래스(즉, table)에 제공할 동작을 정의함*/