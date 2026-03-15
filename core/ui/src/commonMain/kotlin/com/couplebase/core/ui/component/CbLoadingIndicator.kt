package com.couplebase.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CbLoadingIndicator(
    modifier: Modifier = Modifier,
    fullScreen: Boolean = false,
) {
    Box(
        modifier = if (fullScreen) modifier.fillMaxSize() else modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
