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

class CreateCoupleComponent(
    componentContext: ComponentContext,
    private val coupleRepository: CoupleRepository,
    private val onCoupleCreated: (inviteCode: String) -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { CreateHandler(coupleRepository) }

    val state: StateFlow<CreateState> = handler.state

    fun onCreateClicked() {
        handler.createCouple(onCoupleCreated)
    }

    fun onBackClicked() = onBack()

    fun onErrorDismissed() {
        handler.updateState { it.copy(error = null) }
    }
}

data class CreateState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

private class CreateHandler(
    private val coupleRepository: CoupleRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(CreateState())
    val state: StateFlow<CreateState> = _state.asStateFlow()

    fun updateState(transform: (CreateState) -> CreateState) {
        _state.update(transform)
    }

    fun createCouple(onSuccess: (String) -> Unit) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = coupleRepository.createCouple()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess(result.data.inviteCode)
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message ?: "Failed to create couple space")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
