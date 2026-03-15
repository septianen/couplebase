package com.couplebase.feature.wedding.guests.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.model.Guest

class UpdateGuestUseCase(
    private val repository: GuestRepository,
) {
    suspend operator fun invoke(guest: Guest): Result<Guest> =
        repository.upsert(guest)
}
