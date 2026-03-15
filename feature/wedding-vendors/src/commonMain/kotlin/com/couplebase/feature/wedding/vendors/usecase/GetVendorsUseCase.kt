package com.couplebase.feature.wedding.vendors.usecase

import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.core.model.Vendor
import kotlinx.coroutines.flow.Flow

class GetVendorsUseCase(private val repository: VendorRepository) {
    operator fun invoke(coupleId: String): Flow<List<Vendor>> =
        repository.vendorsFlow(coupleId)
}
