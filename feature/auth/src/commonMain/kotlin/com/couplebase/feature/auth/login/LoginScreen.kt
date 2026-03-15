package com.couplebase.feature.auth.login

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.couplebase.core.ui.component.CbButton
import com.couplebase.core.ui.component.CbButtonStyle
import com.couplebase.core.ui.component.CbLoadingIndicator
import com.couplebase.core.ui.component.CbTextField

@Composable
fun LoginScreen(component: LoginComponent) {
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
                    text = "couplebase",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome back",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(48.dp))

                CbTextField(
                    value = state.email,
                    onValueChange = component::onEmailChanged,
                    label = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.emailError,
                    keyboardType = KeyboardType.Email,
                )
                Spacer(modifier = Modifier.height(16.dp))

                CbTextField(
                    value = state.password,
                    onValueChange = component::onPasswordChanged,
                    label = "Password",
                    modifier = Modifier.fillMaxWidth(),
                    error = state.passwordError,
                    isPassword = true,
                )
                Spacer(modifier = Modifier.height(32.dp))

                CbButton(
                    text = "Log In",
                    onClick = component::onLoginClicked,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))

                CbButton(
                    text = "Don't have an account? Sign Up",
                    onClick = component::onSignupClicked,
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
