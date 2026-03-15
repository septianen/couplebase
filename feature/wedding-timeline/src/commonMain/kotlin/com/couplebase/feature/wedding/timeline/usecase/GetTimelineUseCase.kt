package com.couplebase.feature.wedding.timeline.usecase

import com.couplebase.core.domain.repository.TimelineRepository
import com.couplebase.core.model.TimelineBlock
import kotlinx.coroutines.flow.Flow

class GetTimelineUseCase(private val repository: TimelineRepository) {
    operator fun invoke(coupleId: String): Flow<List<TimelineBlock>> =
        repository.timelineFlow(coupleId)
}
