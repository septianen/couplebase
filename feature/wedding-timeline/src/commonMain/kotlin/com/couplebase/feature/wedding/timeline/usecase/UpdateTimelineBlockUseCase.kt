package com.couplebase.feature.wedding.timeline.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.TimelineRepository
import com.couplebase.core.model.TimelineBlock

class UpdateTimelineBlockUseCase(private val repository: TimelineRepository) {
    suspend operator fun invoke(block: TimelineBlock): Result<TimelineBlock> =
        repository.upsert(block)
}
