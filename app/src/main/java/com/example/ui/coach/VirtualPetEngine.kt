package com.example.ui.coach

import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction

import com.example.data.model.Mission

object VirtualPetEngine {
    fun calculateMood(transactions: List<Transaction>, goals: List<BudgetGoal>): PetMood {
        val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }
        val rawSavingsRate = if (totalIncome > 0) (totalIncome + totalExpense) / totalIncome else 0.0
        val savingsRate = if (rawSavingsRate.isNaN()) 0.0 else rawSavingsRate

        val totalGoalProgress = if (goals.isEmpty()) 0.0 else goals.map { if (it.targetAmount > 0) it.savedAmount / it.targetAmount else 0.0 }.average()
        
        // Time decay (Hunger simulation)
        val currentTime = System.currentTimeMillis()
        val latestPositiveTx = transactions.filter { it.amount > 0 }.maxByOrNull { it.timestamp }?.timestamp ?: 0L
        val daysSincePositive = if (latestPositiveTx > 0) (currentTime - latestPositiveTx) / (1000 * 60 * 60 * 24) else 10L
        val timeDecay = if (daysSincePositive > 3) (daysSincePositive - 3) * 0.05 else 0.0

        val combinedScore = ((savingsRate + totalGoalProgress) / 2.0) - timeDecay

        return when {
            combinedScore > 0.4 || savingsRate > 0.3 -> PetMood.HAPPY
            combinedScore > 0.1 || savingsRate > 0.0 -> PetMood.IDLE
            else -> PetMood.ANGRY // Depressed / Hungry
        }
    }

    fun calculateXp(transactions: List<Transaction>, goals: List<BudgetGoal>, missions: List<Mission> = emptyList()): Int {
        val goalsCountXp = goals.size * 50
        val goalsSavedXp = goals.sumOf { it.savedAmount }.toInt()
        
        val totalIncome = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.amount < 0 }.sumOf { it.amount }
        val rawSavingsRate = if (totalIncome > 0) (totalIncome + totalExpense) / totalIncome else 0.0
        val savingsRate = if (rawSavingsRate.isNaN() || rawSavingsRate < 0.0) 0.0 else rawSavingsRate
        val savingsXp = (savingsRate * 500).toInt()

        val missionsXp = missions.filter { it.isCompleted }.sumOf { it.xpReward }

        return goalsCountXp + goalsSavedXp + savingsXp + missionsXp
    }

    fun calculateLevel(xp: Int): Int {
        return 1 + (xp / 1000)
    }

    fun calculateXpInLevel(xp: Int): Int {
        return xp % 1000
    }
}
