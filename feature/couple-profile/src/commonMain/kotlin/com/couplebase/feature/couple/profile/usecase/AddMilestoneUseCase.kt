package com.couplebase.feature.couple.profile.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.core.model.Milestone

class AddMilestoneUseCase(
    private val repository: MilestoneRepository,
) {
    suspend operator fun invoke(milestone: Milestone): Result<Milestone> =
        repository.upsert(milestone)
}
