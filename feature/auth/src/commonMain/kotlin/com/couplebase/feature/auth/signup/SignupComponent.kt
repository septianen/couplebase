package com.couplebase.feature.auth.signup

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

class SignupComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val onSignupSuccess: () -> Unit,
    private val onNavigateBack: () -> Unit,
) : ComponentContext by componentContext {

    private val handler = instanceKeeper.getOrCreate { SignupHandler(authRepository) }

    val state: StateFlow<SignupState> = handler.state

    fun onFullNameChanged(name: String) {
        handler.updateState { it.copy(fullName = name, fullNameError = null) }
    }

    fun onEmailChanged(email: String) {
        handler.updateState { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        handler.updateState { it.copy(password = password, passwordError = null) }
    }

    fun onConfirmPasswordChanged(password: String) {
        handler.updateState { it.copy(confirmPassword = password, confirmPasswordError = null) }
    }

    fun onSignupClicked() {
        val s = state.value
        var hasError = false

        val fullNameError = if (s.fullName.isBlank()) { hasError = true; "Name is required" } else null
        val emailError = if (s.email.isBlank()) { hasError = true; "Email is required" }
            else if (!s.email.contains("@")) { hasError = true; "Invalid email" } else null
        val passwordError = if (s.password.length < 6) { hasError = true; "Password must be at least 6 characters" } else null
        val confirmError = if (s.password != s.confirmPassword) { hasError = true; "Passwords don't match" } else null

        if (hasError) {
            handler.updateState {
                it.copy(
                    fullNameError = fullNameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError,
                )
            }
            return
        }

        handler.signup(s.email, s.password, s.fullName, onSignupSuccess)
    }

    fun onBackClicked() {
        onNavigateBack()
    }

    fun onErrorDismissed() {
        handler.updateState { it.copy(error = null) }
    }
}

data class SignupState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private class SignupHandler(
    private val authRepository: AuthRepository,
) : InstanceKeeper.Instance {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    fun updateState(transform: (SignupState) -> SignupState) {
        _state.update(transform)
    }

    fun signup(email: String, password: String, fullName: String, onSuccess: () -> Unit) {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signUp(email, password, fullName)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Signup failed",
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
