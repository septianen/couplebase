package com.couplebase.feature.wedding.checklist.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.ChecklistRepository

class ToggleChecklistItemUseCase(
    private val repository: ChecklistRepository,
) {
    suspend operator fun invoke(id: String, isCompleted: Boolean): Result<Unit> =
        repository.toggleCompleted(id, isCompleted)
}
