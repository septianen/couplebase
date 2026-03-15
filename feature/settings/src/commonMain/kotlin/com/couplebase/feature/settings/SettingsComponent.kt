package com.couplebase.feature.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.Result
import com.couplebase.core.common.toUserMessage
import com.couplebase.core.datastore.PreferencesDataStore
import com.couplebase.core.datastore.UserPreferences
import com.couplebase.core.domain.repository.AuthRepository
import com.couplebase.core.domain.repository.CoupleRepository
import com.couplebase.core.model.Couple
import com.couplebase.core.model.ThemeMode
import com.couplebase.core.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val preferencesDataStore: PreferencesDataStore,
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository,
    val onBack: () -> Unit = {},
    val onLogout: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        SettingsHandler(coupleId, preferencesDataStore, authRepository, coupleRepository)
    }

    val state: StateFlow<SettingsUiState> = handler.state

    fun onThemeSelected(theme: ThemeMode) {
        handler.setTheme(theme)
    }

    fun onToggleNotifications(enabled: Boolean) {
        handler.setNotifications(enabled)
    }

    fun onToggleCheckinReminder(enabled: Boolean) {
        handler.setCheckinReminder(enabled)
    }

    fun onUpdateWeddingDate(date: String) {
        handler.updateWeddingDate(date)
    }

    fun onCopyInviteCode() {
        handler.updateState { it.copy(inviteCodeCopied = true) }
    }

    fun onShowThemePicker() {
        handler.updateState { it.copy(showThemePicker = true) }
    }

    fun onDismissThemePicker() {
        handler.updateState { it.copy(showThemePicker = false) }
    }

    fun onShowWeddingDateEditor() {
        handler.updateState { it.copy(showWeddingDateEditor = true) }
    }

    fun onDismissWeddingDateEditor() {
        handler.updateState { it.copy(showWeddingDateEditor = false) }
    }

    fun onShowLeaveConfirm() {
        handler.updateState { it.copy(showLeaveConfirm = true) }
    }

    fun onDismissLeaveConfirm() {
        handler.updateState { it.copy(showLeaveConfirm = false) }
    }

    fun onLogoutClicked() {
        handler.logout { onLogout() }
    }

    fun onDismissError() {
        handler.updateState { it.copy(error = null) }
    }
}

data class SettingsUiState(
    val user: User? = null,
    val couple: Couple? = null,
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = true,
    val showThemePicker: Boolean = false,
    val showWeddingDateEditor: Boolean = false,
    val showLeaveConfirm: Boolean = false,
    val inviteCodeCopied: Boolean = false,
    val error: String? = null,
)

private class SettingsHandler(
    private val coupleId: String,
    private val preferencesDataStore: PreferencesDataStore,
    private val authRepository: AuthRepository,
    private val coupleRepository: CoupleRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadData()
        scope.launch {
            preferencesDataStore.preferencesFlow().collect { prefs ->
                _state.update { it.copy(preferences = prefs) }
            }
        }
    }

    private fun loadData() {
        scope.launch {
            var error: String? = null
            val user = when (val r = authRepository.getCurrentUser()) {
                is Result.Success -> r.data
                is Result.Error -> { error = r.toUserMessage(); null }
            }
            val couple = when (val r = coupleRepository.getCouple(coupleId)) {
                is Result.Success -> r.data
                is Result.Error -> { error = r.toUserMessage(); null }
            }
            _state.update { it.copy(user = user, couple = couple, isLoading = false, error = error) }
        }
    }

    fun updateState(transform: (SettingsUiState) -> SettingsUiState) {
        _state.update(transform)
    }

    fun setTheme(theme: ThemeMode) {
        scope.launch {
            preferencesDataStore.updatePreferences { it.copy(themeMode = theme) }
        }
        _state.update { it.copy(showThemePicker = false) }
    }

    fun setNotifications(enabled: Boolean) {
        scope.launch {
            preferencesDataStore.updatePreferences { it.copy(notificationsEnabled = enabled) }
        }
    }

    fun setCheckinReminder(enabled: Boolean) {
        scope.launch {
            preferencesDataStore.updatePreferences { it.copy(dailyCheckinReminder = enabled) }
        }
    }

    fun updateWeddingDate(date: String) {
        val couple = _state.value.couple ?: return
        val updated = couple.copy(weddingDate = date)
        scope.launch {
            when (val r = coupleRepository.updateCouple(updated)) {
                is Result.Success -> _state.update { it.copy(couple = r.data, showWeddingDateEditor = false) }
                is Result.Error -> _state.update { it.copy(error = r.toUserMessage(), showWeddingDateEditor = false) }
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        scope.launch {
            preferencesDataStore.clear()
            authRepository.signOut()
            onComplete()
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
