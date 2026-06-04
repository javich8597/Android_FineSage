package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learned_rules")
data class LearnedRule(
    @PrimaryKey val pattern: String, // Cleaned, lowercased concept keyword (e.g. "netflix", "mercadona")
    val category: String,
    val subcategory: String
)
