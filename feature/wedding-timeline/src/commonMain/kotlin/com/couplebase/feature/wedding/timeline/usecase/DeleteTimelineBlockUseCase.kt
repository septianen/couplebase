package com.couplebase.feature.wedding.timeline.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.TimelineRepository

class DeleteTimelineBlockUseCase(private val repository: TimelineRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
