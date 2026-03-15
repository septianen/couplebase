package com.couplebase.feature.wedding.guests.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.GuestRepository

class DeleteGuestUseCase(
    private val repository: GuestRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.delete(id)
}
