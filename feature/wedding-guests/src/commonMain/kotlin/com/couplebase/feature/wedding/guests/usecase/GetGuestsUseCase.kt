package com.couplebase.feature.wedding.guests.usecase

import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.model.Guest
import kotlinx.coroutines.flow.Flow

class GetGuestsUseCase(
    private val repository: GuestRepository,
) {
    operator fun invoke(coupleId: String): Flow<List<Guest>> =
        repository.guestsFlow(coupleId)
}
