package com.couplebase.feature.couple.profile.usecase

import com.couplebase.core.domain.repository.MilestoneRepository
import com.couplebase.core.model.Milestone
import kotlinx.coroutines.flow.Flow

class GetMilestonesUseCase(
    private val repository: MilestoneRepository,
) {
    operator fun invoke(coupleId: String): Flow<List<Milestone>> =
        repository.milestonesFlow(coupleId)
}
