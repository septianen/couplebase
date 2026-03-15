package com.couplebase.feature.wedding.vendors.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.VendorRepository
import com.couplebase.core.model.VendorPayment

class AddVendorPaymentUseCase(private val repository: VendorRepository) {
    suspend operator fun invoke(payment: VendorPayment): Result<VendorPayment> =
        repository.upsertPayment(payment)
}
