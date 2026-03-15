package com.couplebase.feature.couple.goals.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.LifeGoalRepository

class DeleteGoalUseCase(
    private val repository: LifeGoalRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteGoal(id)
}
