package com.couplebase.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class CbButtonStyle { PRIMARY, OUTLINED, TEXT }

@Composable
fun CbButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CbButtonStyle = CbButtonStyle.PRIMARY,
    enabled: Boolean = true,
) {
    when (style) {
        CbButtonStyle.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }

        CbButtonStyle.OUTLINED -> OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.medium,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }

        CbButtonStyle.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
