package com.couplebase.feature.wedding.checklist.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.model.ChecklistItem

class AddChecklistItemUseCase(
    private val repository: ChecklistRepository,
) {
    suspend operator fun invoke(item: ChecklistItem): Result<ChecklistItem> =
        repository.upsert(item)
}
