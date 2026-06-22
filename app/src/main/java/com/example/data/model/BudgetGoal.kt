package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double,
    val targetDate: String, // format YYYY-MM-DD
    val category: String, // e.g., "General", "Retiro", "Vacaciones"
    val isAutoCalculated: Boolean = true
)
