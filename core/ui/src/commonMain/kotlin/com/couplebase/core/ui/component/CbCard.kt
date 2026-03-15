package com.couplebase.core.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CbCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            content = content,
        )
    } else {
        ElevatedCard(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            content = content,
        )
    }
}
