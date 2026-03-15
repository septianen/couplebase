package com.couplebase.feature.wedding.checklist.usecase

import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.model.ChecklistItem
import kotlinx.coroutines.flow.Flow

class GetChecklistItemsUseCase(
    private val repository: ChecklistRepository,
) {
    operator fun invoke(coupleId: String): Flow<List<ChecklistItem>> =
        repository.checklistFlow(coupleId)
}
