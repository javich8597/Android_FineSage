package com.example.data.local

import androidx.room.*
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.data.model.LearnedRule
import com.example.data.model.CategoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Transactions Queries ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isAnomaly = 1 ORDER BY timestamp DESC")
    fun getAnomalies(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isMicroSpend = 1 ORDER BY timestamp DESC")
    fun getMicroSpends(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    // --- Budget Goals Queries ---
    @Query("SELECT * FROM budget_goals")
    fun getAllGoals(): Flow<List<BudgetGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoal)

    @Query("UPDATE budget_goals SET savedAmount = :saved WHERE id = :id")
    suspend fun updateGoalSavings(id: Int, saved: Double)

    @Delete
    suspend fun deleteGoal(goal: BudgetGoal)

    // --- Dynamic Category Management ---
    @Query("SELECT * FROM category_items ORDER BY category ASC, subcategory ASC")
    fun getAllCategoryItems(): Flow<List<CategoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryItem(item: CategoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryItems(items: List<CategoryItem>)

    @Delete
    suspend fun deleteCategoryItem(item: CategoryItem)

    // --- AI/Learned Categorization Rules ---
    @Query("SELECT * FROM learned_rules")
    fun getAllRules(): Flow<List<LearnedRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: LearnedRule)

    @Delete
    suspend fun deleteRule(rule: LearnedRule)
}
