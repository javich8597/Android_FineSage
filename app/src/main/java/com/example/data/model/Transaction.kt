package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double, // Negative for spending, positive for income
    val concept: String,
    val category: String, // "Alimentos", "Suscripción", "Transporte", "Restaurantes", "Ocio", "Salud", "Ingreso"
    val subcategory: String = "",
    val bankName: String, // "Revolut", "TradeRepublic", "Santander", "Manual"
    val currency: String, // "EUR", "USD", "GBP"
    val timestamp: Long,
    val isAnomaly: Boolean = false,
    val anomalyReason: String? = null,
    val isMicroSpend: Boolean = false,
    val isSynced: Boolean = false
)
