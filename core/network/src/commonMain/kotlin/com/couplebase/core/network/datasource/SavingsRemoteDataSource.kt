package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.SavingsContributionDto
import com.couplebase.core.network.dto.SavingsGoalDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class SavingsRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val goalTable get() = client.postgrest["savings_goals"]
    private val contributionTable get() = client.postgrest["savings_contributions"]

    // --- Savings Goals ---

    suspend fun getGoalsByCoupleId(coupleId: String): List<SavingsGoalDto> =
        goalTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("title", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertGoal(dto: SavingsGoalDto): SavingsGoalDto =
        goalTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun deleteGoal(id: String) {
        goalTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }

    // --- Contributions ---

    suspend fun getContributions(goalId: String): List<SavingsContributionDto> =
        contributionTable.select {
            filter { eq("goal_id", goalId) }
            filter { eq("is_deleted", false) }
            order("date", Order.DESCENDING)
        }.decodeList()

    suspend fun upsertContribution(dto: SavingsContributionDto): SavingsContributionDto =
        contributionTable.upsert(dto) {
            select()
        }.decodeSingle()

    suspend fun deleteContribution(id: String) {
        contributionTable.update({
            set("is_deleted", true)
        }) {
            filter { eq("id", id) }
        }
    }
}
