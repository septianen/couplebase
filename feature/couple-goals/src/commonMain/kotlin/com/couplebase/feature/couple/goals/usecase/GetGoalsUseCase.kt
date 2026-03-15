package com.couplebase.feature.couple.goals.usecase

import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.model.LifeGoal
import kotlinx.coroutines.flow.Flow

class GetGoalsUseCase(
    private val repository: LifeGoalRepository,
) {
    operator fun invoke(coupleId: String): Flow<List<LifeGoal>> =
        repository.goalsFlow(coupleId)
}
