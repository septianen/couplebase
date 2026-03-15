package com.couplebase.feature.couple.profile.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.MilestoneRepository

class DeleteMilestoneUseCase(
    private val repository: MilestoneRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
