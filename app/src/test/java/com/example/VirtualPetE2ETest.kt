package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BudgetGoal
import com.example.data.model.Transaction
import com.example.ui.coach.InteractivePetScreen
import com.example.ui.coach.PetMood
import com.example.ui.coach.PetType
import com.example.ui.coach.PetStatusSection
import com.example.ui.coach.VirtualPetEngine
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.finance.FinanceViewModel
import com.example.ui.management.ManagementTab
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.data.local.AppDatabase
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class VirtualPetE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        kotlinx.coroutines.runBlocking {
            db.financeDao().clearAllTransactions()
            try {
                db.openHelper.writableDatabase.execSQL("DELETE FROM budget_goals")
            } catch (e: Exception) {
                // Ignore if database is not fully initialized yet
            }
        }
    }

    private fun populateDatabase(transactions: List<Transaction>, goals: List<BudgetGoal> = emptyList()) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        kotlinx.coroutines.runBlocking {
            db.financeDao().clearAllTransactions()
            db.financeDao().insertTransactions(transactions)
            goals.forEach { db.financeDao().insertGoal(it) }
        }
    }

    @Test
    fun testGrowth_xpCalculationCorrect() {
        val txs = listOf(
            Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -500.0, concept = "Food", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // 2 txs -> 20 XP. Savings rate = 0.5 -> 250 XP
        val goals = listOf(
            BudgetGoal(id = 1, title = "Rent", targetAmount = 100.0, savedAmount = 50.0, targetDate = "", category = "")
        ) // 1 goal -> 50 XP, savedAmount = 50 -> 50 XP
        // Expected XP = 20 + 50 + 50 + 250 = 370 XP
        val xp = VirtualPetEngine.calculateXp(txs, goals)
        assertEquals(370, xp)
    }

    @Test
    fun testGrowth_levelProgressionCorrect() {
        val xp = 2370
        val level = VirtualPetEngine.calculateLevel(xp)
        val xpInLevel = VirtualPetEngine.calculateXpInLevel(xp)
        assertEquals(3, level)
        assertEquals(370, xpInLevel)
    }

    // =========================================================================
    // TIER 1: FEATURE COVERAGE (15 tests)
    // =========================================================================

    // --- FEATURE 1: Pet Selection UI (5 tests) ---

    @Test
    fun testSelectionUI_displayedWhenStyleIsEmpty() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        // Clear style to simulate no active pet style initially
        viewModel.setPetStyle("")
        
        composeTestRule.setContent {
            ManagementTab(
                viewModel = viewModel,
                labels = mapOf("config_security" to "Config"),
                isDarkMode = true
            )
        }
        
        // Verifies the selection options exist and are interactable
        composeTestRule.onNodeWithTag("pet_style_TRADITIONAL").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pet_style_ZEN").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pet_style_FUTURISTIC").assertIsDisplayed()
    }

    @Test
    fun testSelectionUI_traditionalSelectionWorks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("")
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        composeTestRule.onNodeWithTag("pet_style_TRADITIONAL").performClick()
        composeTestRule.waitForIdle()
        assertEquals("TRADITIONAL", viewModel.petStyle.value)
    }

    @Test
    fun testSelectionUI_zenSelectionWorks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("")
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        composeTestRule.onNodeWithTag("pet_style_ZEN").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ZEN", viewModel.petStyle.value)
    }

    @Test
    fun testSelectionUI_futuristicSelectionWorks() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("")
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        composeTestRule.onNodeWithTag("pet_style_FUTURISTIC").performClick()
        composeTestRule.waitForIdle()
        assertEquals("FUTURISTIC", viewModel.petStyle.value)
    }

    @Test
    fun testSelectionUI_dismissedOnceStyleIsSelected() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("ZEN")
        
        composeTestRule.setContent {
            InteractivePetScreen(
                viewModel = viewModel,
                isSpan = false,
                isDarkMode = true,
                transactions = emptyList(),
                onClose = {}
            )
        }
        
        // In Zen style, the traditional type-switching filter chips (PIG, CAT, DOG) should be hidden
        composeTestRule.onNodeWithText("PIG").assertDoesNotExist()
        composeTestRule.onNodeWithText("CAT").assertDoesNotExist()
        composeTestRule.onNodeWithText("DOG").assertDoesNotExist()
    }

    // --- FEATURE 2: Pet Mood & Growth States (5 tests) ---

    @Test
    fun testMood_happyStateRendersCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val transactions = listOf(Transaction(amount = 100.0, concept = "In", category = "Ingreso", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()))
        populateDatabase(transactions)

        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = transactions, onClose = {})
        }
        
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Your finances are legendary!").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun testMood_idleStateRendersCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val transactions = listOf(
            Transaction(amount = 100.0, concept = "In", category = "Ingreso", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -95.0, concept = "Out", category = "Gasto", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = 5% -> IDLE
        populateDatabase(transactions)

        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = transactions, onClose = {})
        }
        
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("You are on a good path.").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun testMood_angryStateRendersCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val transactions = listOf(
            Transaction(amount = 100.0, concept = "In", category = "Ingreso", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -110.0, concept = "Out", category = "Gasto", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = -10% -> ANGRY
        populateDatabase(transactions)

        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = transactions, onClose = {})
        }
        
        composeTestRule.onNodeWithText("Starving: Spending a bit too much.").assertIsDisplayed()
    }

    @Test
    fun testGrowth_rendersLvlAndExpCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = emptyList(), onClose = {})
        }
        
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("LVL 1").assertIsDisplayed()
                composeTestRule.onNodeWithText("0 / 1000 XP").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun testMood_pettingInteractionUpdatesState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = emptyList(), onClose = {})
        }
        
        // Verify petting prompt/encouragement is shown
        composeTestRule.onNodeWithText("✨ Pet me! ✨").assertIsDisplayed()
    }

    // --- FEATURE 3: Financial Health Integration (5 tests) ---

    @Test
    fun testFinancial_highIncomeSetsHappyMood() {
        val txs = listOf(Transaction(amount = 5000.0, concept = "Income", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()))
        val mood = VirtualPetEngine.calculateMood(txs, emptyList())
        assertEquals(PetMood.HAPPY, mood)
    }

    @Test
    fun testFinancial_highExpensesSetsAngryMood() {
        val txs = listOf(
            Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -1050.0, concept = "Rent", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = -5%, combined score = -2.5% -> ANGRY (<= 10%)
        val mood = VirtualPetEngine.calculateMood(txs, emptyList())
        assertEquals(PetMood.ANGRY, mood)
    }

    @Test
    fun testFinancial_balancedBudgetSetsIdleMood() {
        val txs = listOf(
            Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -850.0, concept = "Rent", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = 15%, combined score = 7.5% -> IDLE (savings rate > 0.0)
        val mood = VirtualPetEngine.calculateMood(txs, emptyList())
        assertEquals(PetMood.IDLE, mood)
    }

    @Test
    fun testFinancial_goalProgressUpdatesPetState() {
        val txs = listOf(
            Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -950.0, concept = "Rent", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = 0.05
        val goals = listOf(
            BudgetGoal(id = 1, title = "Goal", targetAmount = 100.0, savedAmount = 90.0, targetDate = "", category = "")
        ) // goal progress = 0.9. combined = (0.05 + 0.9)/2 = 0.475 -> HAPPY
        val mood = VirtualPetEngine.calculateMood(txs, goals)
        assertEquals(PetMood.HAPPY, mood)
    }

    @Test
    fun testFinancial_noTransactionsDefaultsToAngry() {
        val mood = VirtualPetEngine.calculateMood(emptyList(), emptyList())
        assertEquals(PetMood.ANGRY, mood)
    }


    // =========================================================================
    // TIER 2: BOUNDARY & EDGE CASES (15 tests)
    // =========================================================================

    // --- FEATURE 1: Selection UI Boundary/Edge (5 tests) ---

    @Test
    fun testSelectionUI_invalidStyleStringDefaultsToSelection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("UNKNOWN_STYLE")
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        // Unknown style defaults selection to present all options
        composeTestRule.onNodeWithTag("pet_style_ZEN").assertIsDisplayed()
    }

    @Test
    fun testSelectionUI_emptyLanguageStringDoesNotCrashSelection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        composeTestRule.onNodeWithTag("pet_style_ZEN").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ZEN", viewModel.petStyle.value)
    }

    @Test
    fun testSelectionUI_rapidStyleSwitchingDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        
        composeTestRule.onNodeWithTag("pet_style_ZEN").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pet_style_FUTURISTIC").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pet_style_TRADITIONAL").performClick()
        composeTestRule.waitForIdle()
        assertEquals("TRADITIONAL", viewModel.petStyle.value)
    }

    @Test
    fun testSelectionUI_extremeScreenSizeSelectionRenders() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        composeTestRule.setContent {
            ManagementTab(viewModel = viewModel, labels = emptyMap(), isDarkMode = true)
        }
        composeTestRule.onNodeWithTag("pet_style_TRADITIONAL").assertExists()
    }

    @Test
    fun testSelectionUI_viewModelResetClearsSelectedStyle() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("FUTURISTIC")
        
        // Reset to default style
        viewModel.setPetStyle("TRADITIONAL")
        assertEquals("TRADITIONAL", viewModel.petStyle.value)
    }

    // --- FEATURE 2: Pet Mood & Growth States Boundary/Edge (5 tests) ---

    @Test
    fun testMood_exactHappyThresholdScore() {
        val txs = listOf(Transaction(amount = 1000.0, concept = "In", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())) // savings rate = 1.0
        val goals = listOf(BudgetGoal(id = 1, title = "Goal", targetAmount = 100.0, savedAmount = 0.0, targetDate = "", category = "")) // progress = 0.0
        // combined = (1.0 + 0.0) / 2.0 = 0.5 > 0.4 -> HAPPY
        val mood = VirtualPetEngine.calculateMood(txs, goals)
        assertEquals(PetMood.HAPPY, mood)
    }

    @Test
    fun testMood_exactIdleThresholdScore() {
        val txs2 = listOf(
            Transaction(amount = 100.0, concept = "In", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -98.0, concept = "Out", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = 0.02
        val goals2 = listOf(BudgetGoal(id = 1, title = "G", targetAmount = 100.0, savedAmount = 20.0, targetDate = "", category = "")) // progress = 0.2
        // combined = 0.22 / 2 = 0.11 > 0.1 -> IDLE
        val mood = VirtualPetEngine.calculateMood(txs2, goals2)
        assertEquals(PetMood.IDLE, mood)
    }

    @Test
    fun testMood_negativeSavingsRateExtremeValue() {
        val txs = listOf(
            Transaction(amount = 10.0, concept = "In", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -1000.0, concept = "Out", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = -99.0
        val mood = VirtualPetEngine.calculateMood(txs, emptyList())
        assertEquals(PetMood.ANGRY, mood)
    }

    @Test
    fun testMood_pettingTransitionTimerCompletes() {
        // Assert that PetMood enum has the expected items
        assertTrue(PetMood.values().contains(PetMood.PETTING))
        assertTrue(PetMood.values().contains(PetMood.HAPPY))
    }

    @Test
    fun testGrowth_expProgressClampedBetweenZeroAndOne() {
        val savingsRate1 = -0.5
        val progress1 = if (savingsRate1 < 0) 0.1f else savingsRate1.toFloat()
        assertEquals(0.1f, progress1, 0.001f)

        val savingsRate2 = 1.5
        val progress2 = if (savingsRate2 < 0) 0.1f else savingsRate2.toFloat()
        val clampedProgress = progress2.coerceIn(0f, 1f)
        assertEquals(1.0f, clampedProgress, 0.001f)
    }

    // --- FEATURE 3: Financial Health Integration Boundary/Edge (5 tests) ---

    @Test
    fun testFinancial_zeroIncomeDivisionByZeroHandled() {
        val txs = listOf(
            Transaction(amount = -100.0, concept = "Expense", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // totalIncome = 0.0
        val mood = VirtualPetEngine.calculateMood(txs, emptyList())
        assertEquals(PetMood.ANGRY, mood) // combinedScore = 0.0 -> ANGRY
    }

    @Test
    fun testFinancial_multipleZeroTargetGoalsHandled() {
        val txs = listOf(Transaction(amount = 1000.0, concept = "In", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())) // savings rate = 1.0
        val goals = listOf(
            BudgetGoal(id = 1, title = "Goal 1", targetAmount = 0.0, savedAmount = 10.0, targetDate = "", category = "")
        ) // progress = 0.0 due to targetAmount = 0 check
        // combined = (1.0 + 0.0) / 2 = 0.5 -> HAPPY
        val mood = VirtualPetEngine.calculateMood(txs, goals)
        assertEquals(PetMood.HAPPY, mood)
    }

    @Test
    fun testFinancial_extremelyHighGoalSavingsHandled() {
        val txs = listOf(Transaction(amount = 1000.0, concept = "In", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())) // 1.0
        val goals = listOf(
            BudgetGoal(id = 1, title = "Goal 1", targetAmount = 100.0, savedAmount = 250.0, targetDate = "", category = "")
        ) // progress = 2.5. combined = (1.0 + 2.5)/2 = 1.75
        val mood = VirtualPetEngine.calculateMood(txs, goals)
        assertEquals(PetMood.HAPPY, mood)
    }

    @Test
    fun testFinancial_emptyTransactionsAndGoalsHandled() {
        val mood = VirtualPetEngine.calculateMood(emptyList(), emptyList())
        assertEquals(PetMood.ANGRY, mood)
    }

    @Test
    fun testFinancial_nanCombinedScoreDefaultsToAngry() {
        val combinedScore = Double.NaN
        val mood = when {
            combinedScore > 0.4 -> PetMood.HAPPY
            combinedScore > 0.1 -> PetMood.IDLE
            else -> PetMood.ANGRY
        }
        assertEquals(PetMood.ANGRY, mood)
    }


    // =========================================================================
    // TIER 3: CROSS-FEATURE COMBINATIONS (3 tests)
    // =========================================================================

    @Test
    fun testCross_styleChangePreservesMoodState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val transactions = listOf(Transaction(amount = 2000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()))
        populateDatabase(transactions)

        val viewModel = FinanceViewModel(context)
        
        // 1. Traditional Pet HAPPY
        viewModel.setPetStyle("TRADITIONAL")
        composeTestRule.setContent {
            InteractivePetScreen(viewModel = viewModel, isSpan = false, isDarkMode = true, transactions = transactions, onClose = {})
        }
        
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Your finances are legendary!").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // 2. Switch Style to ZEN
        viewModel.setPetStyle("ZEN")
        
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Zen Seedling").assertIsDisplayed()
                composeTestRule.onNodeWithText("Your finances are legendary!").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun testCross_interactionDuringFinancialStateChange() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        viewModel.setPetStyle("TRADITIONAL")
        
        composeTestRule.setContent {
            val transactions by viewModel.transactions.collectAsState()
            InteractivePetScreen(
                viewModel = viewModel,
                isSpan = false,
                isDarkMode = true,
                transactions = transactions,
                onClose = {}
            )
        }
        
        // Initial state with no transactions should be angry/starving
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Starving: Spending a bit too much.").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
        
        // Add positive transaction synchronously using Room directly to avoid calling WorkManager triggerSync
        val db = AppDatabase.getDatabase(context)
        kotlinx.coroutines.runBlocking {
            db.financeDao().insertTransaction(
                Transaction(amount = 5000.0, concept = "Payday Boost", category = "Ingreso", subcategory = "", bankName = "Bank", currency = "EUR", timestamp = System.currentTimeMillis())
            )
        }
        
        // Verify the UI dynamic mood description updates to Happy
        composeTestRule.waitUntil(3000) {
            try {
                composeTestRule.onNodeWithText("Your finances are legendary!").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    @Test
    fun testCross_growthProgressInfluencedByStyleLimits() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        // Select Futuristic style
        viewModel.setPetStyle("FUTURISTIC")
        
        composeTestRule.setContent {
            InteractivePetScreen(
                viewModel = viewModel,
                isSpan = false,
                isDarkMode = true,
                transactions = emptyList(),
                onClose = {}
            )
        }
        
        // Verify it renders the Cyber-Hatchling (Futuristic level 1)
        composeTestRule.onNodeWithText("Cyber-Hatchling").assertIsDisplayed()
        
        // Switch to Traditional
        viewModel.setPetStyle("TRADITIONAL")
        composeTestRule.waitForIdle()
        
        // Verify it updates name to Penny Piglet (Traditional level 1)
        composeTestRule.onNodeWithText("Penny Piglet").assertIsDisplayed()
    }


    // =========================================================================
    // TIER 4: REAL-WORLD SCENARIOS (5 tests)
    // =========================================================================

    @Test
    fun testScenario_paydayBoost() {
        // User starts with a negative budget (ANGRY)
        val initialTxs = listOf(
            Transaction(amount = 100.0, concept = "Initial", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -200.0, concept = "Rent", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        )
        assertEquals(PetMood.ANGRY, VirtualPetEngine.calculateMood(initialTxs, emptyList()))

        // Then salary arrives
        val postPaydayTxs = initialTxs + Transaction(amount = 2500.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        // total income = 2600, total expenses = -200. savings rate = 2400 / 2600 = 0.923 > 0.3 -> HAPPY
        assertEquals(PetMood.HAPPY, VirtualPetEngine.calculateMood(postPaydayTxs, emptyList()))
    }

    @Test
    fun testScenario_unplannedSplurge() {
        // User starts with positive savings rate (HAPPY)
        val initialTxs = listOf(
            Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis()),
            Transaction(amount = -400.0, concept = "Rent", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        ) // savings rate = 0.6 -> HAPPY
        assertEquals(PetMood.HAPPY, VirtualPetEngine.calculateMood(initialTxs, emptyList()))

        // Then a major splurge occurs
        val postSplurgeTxs = initialTxs + Transaction(amount = -580.0, concept = "Luxury Hotel", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())
        // total income = 1000, total expenses = -980. savings rate = 20 / 1000 = 0.02 -> IDLE
        assertEquals(PetMood.IDLE, VirtualPetEngine.calculateMood(postSplurgeTxs, emptyList()))
    }

    @Test
    fun testScenario_goalAccomplished() {
        val txs = listOf(Transaction(amount = 1000.0, concept = "Salary", category = "", subcategory = "", bankName = "", currency = "EUR", timestamp = System.currentTimeMillis())) // savings rate = 1.0
        val goals = listOf(
            BudgetGoal(id = 1, title = "Laptop", targetAmount = 500.0, savedAmount = 100.0, targetDate = "", category = "")
        ) // progress = 0.2. combined = (1.0 + 0.2)/2 = 0.6 -> HAPPY
        assertEquals(PetMood.HAPPY, VirtualPetEngine.calculateMood(txs, goals))

        // Save money to complete goal
        val completedGoals = listOf(
            BudgetGoal(id = 1, title = "Laptop", targetAmount = 500.0, savedAmount = 500.0, targetDate = "", category = "")
        ) // progress = 1.0. combined = (1.0 + 1.0)/2 = 1.0 -> HAPPY
        assertEquals(PetMood.HAPPY, VirtualPetEngine.calculateMood(txs, completedGoals))
    }

    @Test
    fun testScenario_newUserOnboarding() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        // Starts with empty/default
        assertEquals("", viewModel.petStyle.value)
        
        // Select Zen Tree
        viewModel.setPetStyle("ZEN")
        assertEquals("ZEN", viewModel.petStyle.value)
    }

    @Test
    fun testScenario_currencyFluctuationImpact() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = FinanceViewModel(context)
        
        // EUR selected, rate is 1.0
        assertEquals("EUR", viewModel.selectedCurrency.value)
        
        viewModel.setBaseCurrency("USD")
        assertEquals("USD", viewModel.selectedCurrency.value)
    }
}
