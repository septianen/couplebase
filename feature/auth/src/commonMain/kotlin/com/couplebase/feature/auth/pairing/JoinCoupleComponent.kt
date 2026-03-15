package com.couplebase.feature.auth.pairing

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.CoupleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class JoinCoupleComponent(
    componentContext: ComponentContext,
    private val coupleRepository: CoupleRepository,
    private val onJoinSuccess: () -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { JoinHandler(coupleRepository) }

    val state: StateFlow<JoinState> = handler.state

    fun onCodeChanged(index: Int, char: String) {
        val current = state.value.codeDigits.toMutableList()
        current[index] = char.take(1)
        handler.updateState { it.copy(codeDigits = current, error = null) }
    }

    fun onJoinClicked() {
        val code = state.value.codeDigits.joinToString("")
        if (code.length < 6) {
            handler.updateState { it.copy(error = "Please enter the full 6-character code") }
            return
        }
        handler.joinCouple(code, onJoinSuccess)
    }

    fun onBackClicked() = onBack()

    fun onErrorDismissed() {
        handler.updateState { it.copy(error = null) }
    }
}

data class JoinState(
    val codeDigits: List<String> = List(6) { "" },
    val isLoading: Boolean = false,
    val error: String? = null,
)

private class JoinHandler(
    private val coupleRepository: CoupleRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(JoinState())
    val state: StateFlow<JoinState> = _state.asStateFlow()

    fun updateState(transform: (JoinState) -> JoinState) {
        _state.update(transform)
    }

    fun joinCouple(inviteCode: String, onSuccess: () -> Unit) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = coupleRepository.joinCouple(inviteCode)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message ?: "Invalid invite code")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
