package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_items")
data class CategoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val subcategory: String
)
