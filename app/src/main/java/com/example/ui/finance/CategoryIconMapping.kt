package com.example.ui.finance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("alimento") || lower.contains("supermercado") || lower.contains("comida") || lower.contains("food") || lower.contains("groceries") -> Icons.Filled.ShoppingCart
        lower.contains("restaurante") || lower.contains("bar") || lower.contains("café") || lower.contains("cafe") || lower.contains("restaurant") -> Icons.Filled.Restaurant
        lower.contains("transporte") || lower.contains("gasolina") || lower.contains("coche") || lower.contains("transport") -> Icons.Filled.DirectionsCar
        lower.contains("suscripción") || lower.contains("suscripcion") || lower.contains("cuota") || lower.contains("subscription") -> Icons.Filled.EventRepeat
        lower.contains("ocio") || lower.contains("entretenimiento") || lower.contains("cine") || lower.contains("juegos") || lower.contains("leisure") -> Icons.Filled.Movie
        lower.contains("salud") || lower.contains("farmacia") || lower.contains("médico") || lower.contains("health") -> Icons.Filled.LocalHospital
        lower.contains("hogar") || lower.contains("casa") || lower.contains("alquiler") || lower.contains("home") || lower.contains("rent") -> Icons.Filled.Home
        lower.contains("servicios") || lower.contains("facturas") || lower.contains("luz") || lower.contains("agua") || lower.contains("internet") || lower.contains("bills") -> Icons.Filled.Receipt
        lower.contains("ingreso") || lower.contains("nómina") || lower.contains("salario") || lower.contains("income") || lower.contains("salary") -> Icons.Filled.AttachMoney
        lower.contains("inversión") || lower.contains("inversiones") || lower.contains("ahorro") || lower.contains("investment") -> Icons.Filled.TrendingUp
        lower.contains("ropa") || lower.contains("compras") || lower.contains("shopping") || lower.contains("clothing") -> Icons.Filled.Checkroom
        lower.contains("educación") || lower.contains("libros") || lower.contains("escuela") || lower.contains("education") -> Icons.Filled.School
        lower.contains("viaje") || lower.contains("vacaciones") || lower.contains("vuelo") || lower.contains("travel") -> Icons.Filled.Flight
        else -> Icons.Filled.Category
    }
}
