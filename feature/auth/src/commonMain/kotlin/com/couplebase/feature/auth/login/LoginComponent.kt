package com.couplebase.feature.auth.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.couplebase.core.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.couplebase.core.common.Result

class LoginComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val onLoginSuccess: () -> Unit,
    private val onNavigateToSignup: () -> Unit,
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { LoginHandler(authRepository) }

    val state: StateFlow<LoginState> = handler.state

    fun onEmailChanged(email: String) {
        handler.updateState { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        handler.updateState { it.copy(password = password, passwordError = null) }
    }

    fun onLoginClicked() {
        val currentState = state.value
        val emailError = if (currentState.email.isBlank()) "Email is required" else null
        val passwordError = if (currentState.password.isBlank()) "Password is required" else null

        if (emailError != null || passwordError != null) {
            handler.updateState { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        handler.login(currentState.email, currentState.password, onLoginSuccess)
    }

    fun onSignupClicked() {
        onNavigateToSignup()
    }

    fun onErrorDismissed() {
        handler.updateState { it.copy(error = null) }
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private class LoginHandler(
    private val authRepository: AuthRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateState(transform: (LoginState) -> LoginState) {
        _state.update(transform)
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Login failed",
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
    }
}
