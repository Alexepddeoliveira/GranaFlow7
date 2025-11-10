package com.alex.granaflow7

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LaunchDao {

    @Insert
    fun insert(launch: LaunchEntity)

    @Query("SELECT * FROM launches ORDER BY id DESC")
    fun getAll(): List<LaunchEntity>

    @Query("SELECT SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END) FROM launches")
    fun totalIncome(): Double?

    @Query("SELECT SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END) FROM launches")
    fun totalExpense(): Double?
}
