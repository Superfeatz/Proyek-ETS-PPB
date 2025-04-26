package com.example.mymoneynotes.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "INCOME" atau "EXPENSE"
    val category: String,
    val amount: Double,
    val description: String,
    val date: Long = System.currentTimeMillis()
)