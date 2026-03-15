package com.couplebase.feature.comm.checkin

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.today
import com.couplebase.core.domain.repository.CommunicationRepository
import com.couplebase.core.model.DailyCheckin
import com.couplebase.core.model.Mood
import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.common.nowLocal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CheckinComponent(
    componentContext: ComponentContext,
    private val coupleId: String,
    private val userId: String = "stub-user-id",
    private val repository: CommunicationRepository,
    val onBack: () -> Unit = {},
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate {
        CheckinHandler(coupleId, userId, repository)
    }

    val state: StateFlow<CheckinUiState> = handler.state

    fun onMoodSelected(mood: Mood) {
        handler.selectMood(mood)
    }

    fun onReflectionChanged(text: String) {
        handler.updateReflection(text)
    }

    fun onSaveCheckin() {
        handler.saveCheckin()
    }
}

data class CheckinUiState(
    val todayCheckin: DailyCheckin? = null,
    val partnerCheckin: DailyCheckin? = null,
    val weeklyMoods: List<DailyCheckin> = emptyList(),
    val selectedMood: Mood? = null,
    val reflection: String = "",
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
)

private class CheckinHandler(
    private val coupleId: String,
    private val userId: String,
    private val repository: CommunicationRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    init {
        loadCheckins()
    }

    private fun loadCheckins() {
        scope.launch {
            val todayDate = today()
            val dateStr = todayDate.toString()
            val myCheckin = when (val r = repository.getCheckin(coupleId, userId, dateStr)) {
                is Result.Success -> r.data
                is Result.Error -> null
            }
            val partnerCheckin = when (val r = repository.getPartnerCheckin(coupleId, userId, dateStr)) {
                is Result.Success -> r.data
                is Result.Error -> null
            }
            // Load last 7 days of moods
            @Suppress("OPT_IN_USAGE")
            val weekStart = todayDate.toEpochDays().let { days ->
                kotlinx.datetime.LocalDate.fromEpochDays(days - 6)
            }.toString()
            val weeklyMoods = when (val r = repository.getWeeklyMoods(coupleId, weekStart, dateStr)) {
                is Result.Success -> r.data
                is Result.Error -> emptyList()
            }
            _state.update {
                it.copy(
                    todayCheckin = myCheckin,
                    partnerCheckin = partnerCheckin,
                    weeklyMoods = weeklyMoods,
                    selectedMood = myCheckin?.mood,
                    reflection = myCheckin?.reflection ?: "",
                    isLoading = false,
                    isSaved = myCheckin != null,
                )
            }
        }
    }

    fun selectMood(mood: Mood) {
        _state.update { it.copy(selectedMood = mood, isSaved = false) }
    }

    fun updateReflection(text: String) {
        _state.update { it.copy(reflection = text, isSaved = false) }
    }

    fun saveCheckin() {
        val current = _state.value
        val mood = current.selectedMood ?: return
        val checkin = DailyCheckin(
            id = current.todayCheckin?.id ?: generateUuid(),
            coupleId = coupleId,
            userId = userId,
            date = today().toString(),
            mood = mood,
            reflection = current.reflection.ifBlank { null },
            updatedAt = nowLocal().toString(),
        )
        scope.launch {
            when (val r = repository.upsertCheckin(checkin)) {
                is Result.Success -> {
                    _state.update { it.copy(todayCheckin = r.data, isSaved = true) }
                    loadCheckins()
                }
                is Result.Error -> {}
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
