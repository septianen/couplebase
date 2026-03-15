package com.couplebase.di

import com.couplebase.core.common.Result
import com.couplebase.core.domain.repository.CoupleRepository
import com.couplebase.core.model.Couple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class StubCoupleRepository : CoupleRepository {
    private val coupleFlow = MutableStateFlow<Couple?>(null)

    override fun coupleFlow(coupleId: String): Flow<Couple?> = coupleFlow

    override suspend fun createCouple(): Result<Couple> {
        val couple = Couple(
            id = "stub-couple-id",
            inviteCode = "A7X9K2",
            partner1Id = "stub-user-id",
            createdAt = "",
            updatedAt = "",
        )
        coupleFlow.value = couple
        return Result.Success(couple)
    }

    override suspend fun joinCouple(inviteCode: String): Result<Couple> {
        val couple = Couple(
            id = "stub-couple-id",
            inviteCode = inviteCode,
            partner1Id = "stub-partner-id",
            partner2Id = "stub-user-id",
            createdAt = "",
            updatedAt = "",
        )
        coupleFlow.value = couple
        return Result.Success(couple)
    }

    override suspend fun getCouple(coupleId: String): Result<Couple?> =
        Result.Success(coupleFlow.value)

    override suspend fun updateCouple(couple: Couple): Result<Couple> {
        coupleFlow.value = couple
        return Result.Success(couple)
    }
}
