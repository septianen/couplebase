package com.couplebase.feature.couple.goals.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.LifeGoalRepository
import com.couplebase.core.model.LifeGoal

class AddGoalUseCase(
    private val repository: LifeGoalRepository,
) {
    suspend operator fun invoke(goal: LifeGoal): Result<LifeGoal> =
        repository.upsertGoal(goal)
}
