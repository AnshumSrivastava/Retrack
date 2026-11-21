package com.example.retrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.retrack.data.communicator.IUsageDataCommunicator
import com.example.retrack.data.db.RetrackDbHelper
import com.example.retrack.data.repository.RepositoryCommunicatorImpl
import com.example.retrack.data.repository.SettingsRepository
import com.example.retrack.data.repository.UsageRepository
import com.example.retrack.service.CommunicatorImpl
import com.example.retrack.service.QuotaLogicService
import com.example.retrack.service.RetrackAccessibilityService
import com.example.retrack.service.SessionSwipeTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainUiState(
    val dailyTotal: Int = 0,
    val dailyLimit: Int = 20,
    val streakDays: Int = 0,
    val platformBreakdown: Map<String, Int> = emptyMap(),
    val motivationalMessage: String = "IT'S TIME TO FOCUS ON WHAT MATTERS!",
    val habits: List<String> = listOf("Read a book", "Go for a walk", "Drink water", "Meditate"),
    val overlayEnabled: Boolean = true,
    val isAccessibilityActive: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = RetrackDbHelper(application.applicationContext)
    private val settingsRepo = SettingsRepository(dbHelper)
    private val usageRepo = UsageRepository(dbHelper)
    private val repoCommunicator = RepositoryCommunicatorImpl(usageRepo, settingsRepo)

    private val defaultCommunicator: IUsageDataCommunicator by lazy {
        RetrackAccessibilityService.communicatorInstance ?: run {
            val sessionTracker = SessionSwipeTracker()
            val quotaService = QuotaLogicService(repoCommunicator, sessionTracker)
            CommunicatorImpl(repoCommunicator, quotaService)
        }
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        _uiState.update { currentState ->
            currentState.copy(
                dailyTotal = activeCommunicator.getDailyTotalCount(),
                dailyLimit = activeCommunicator.getDailyLimit(),
                streakDays = activeCommunicator.getStreakDays(),
                platformBreakdown = activeCommunicator.getPlatformBreakdown(),
                motivationalMessage = activeCommunicator.getMotivationalMessage(),
                habits = activeCommunicator.getHabitsList(),
                overlayEnabled = activeCommunicator.isOverlayBadgeEnabled(),
                isAccessibilityActive = activeCommunicator.isAccessibilityServiceActive()
            )
        }
    }

    fun updateDailyLimit(limit: Int) {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.setDailyLimit(limit)
        refreshState()
    }

    fun updateMotivationalMessage(msg: String) {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.setMotivationalMessage(msg)
        refreshState()
    }

    fun addHabit(habit: String) {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.addHabit(habit)
        refreshState()
    }

    fun removeHabit(habit: String) {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.removeHabit(habit)
        refreshState()
    }

    fun toggleOverlayEnabled(enabled: Boolean) {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.setOverlayBadgeEnabled(enabled)
        refreshState()
    }

    fun resetTodayQuota() {
        val activeCommunicator = RetrackAccessibilityService.communicatorInstance ?: defaultCommunicator
        activeCommunicator.resetTodayQuota()
        refreshState()
    }
}
