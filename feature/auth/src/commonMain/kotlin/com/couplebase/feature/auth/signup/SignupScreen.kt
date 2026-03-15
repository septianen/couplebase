package com.couplebase.feature.auth.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbLoadingIndicator
import com.couplebase.core.ui.component.CbTextField

@Composable
fun SignupScreen(component: SignupComponent) {
    val state by component.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (state.isLoading) {
            CbLoadingIndicator(fullScreen = true)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start planning your forever together",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(36.dp))

                CbTextField(
                    value = state.fullName,
                    onValueChange = component::onFullNameChanged,
                    label = "Full Name",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.fullNameError,
                )
                Spacer(modifier = Modifier.height(12.dp))

                CbTextField(
                    value = state.email,
                    onValueChange = component::onEmailChanged,
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                )
                Spacer(modifier = Modifier.height(12.dp))

                CbTextField(
                    value = state.password,
                    onValueChange = component::onPasswordChanged,
                    label = "Password",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.passwordError,
                    isPassword = true,
                )
                Spacer(modifier = Modifier.height(12.dp))

                CbTextField(
                    value = state.confirmPassword,
                    onValueChange = component::onConfirmPasswordChanged,
                    label = "Confirm Password",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.confirmPasswordError,
                    isPassword = true,
                )
                Spacer(modifier = Modifier.height(32.dp))

                CbButton(
                    text = "Sign Up",
                    onClick = component::onSignupClicked,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))

                CbButton(
                    text = "Already have an account? Log In",
                    onClick = component::onBackClicked,
                    style = CbButtonStyle.TEXT,
                )

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Snackbar(
                        action = {
                            TextButton(onClick = component::onErrorDismissed) {
                                Text("Dismiss")
                            }
                        },
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }
}
