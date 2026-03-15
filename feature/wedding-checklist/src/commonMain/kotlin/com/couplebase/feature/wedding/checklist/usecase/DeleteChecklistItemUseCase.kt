package com.couplebase.feature.wedding.checklist.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.ChecklistRepository

class DeleteChecklistItemUseCase(
    private val repository: ChecklistRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
