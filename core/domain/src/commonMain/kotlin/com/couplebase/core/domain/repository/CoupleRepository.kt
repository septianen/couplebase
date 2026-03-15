package com.couplebase.core.domain.repository

import com.couplebase.core.common.Result
import com.couplebase.core.model.Couple
import kotlinx.coroutines.flow.Flow

interface CoupleRepository {
    fun coupleFlow(coupleId: String): Flow<Couple?>
    suspend fun createCouple(): Result<Couple>
    suspend fun joinCouple(inviteCode: String): Result<Couple>
    suspend fun getCouple(coupleId: String): Result<Couple?>
    suspend fun updateCouple(couple: Couple): Result<Couple>
}
