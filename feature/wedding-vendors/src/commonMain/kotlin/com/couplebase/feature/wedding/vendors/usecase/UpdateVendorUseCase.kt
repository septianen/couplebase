package com.couplebase.feature.wedding.vendors.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.core.model.Vendor

class UpdateVendorUseCase(private val repository: VendorRepository) {
    suspend operator fun invoke(vendor: Vendor): Result<Vendor> =
        repository.upsertVendor(vendor)
}
