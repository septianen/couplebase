package com.couplebase.feature.wedding.vendors.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.VendorRepository

class DeleteVendorUseCase(private val repository: VendorRepository) {
    suspend operator fun invoke(id: String): Result<Unit> =
        repository.deleteVendor(id)
}
