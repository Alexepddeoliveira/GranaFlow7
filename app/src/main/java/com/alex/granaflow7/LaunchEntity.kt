package com.alex.granaflow7

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launches")
data class LaunchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val isPaid: Boolean = false,
    val date: String //dd/MM/yyyy
)
