package com.couplebase.core.network.datasource

import com.couplebase.core.network.SupabaseProvider
import com.couplebase.core.network.dto.GoalMilestoneDto
import com.couplebase.core.network.dto.LifeGoalDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order

class LifeGoalRemoteDataSource {

    private val client get() = SupabaseProvider.getClient()
    private val goalTable get() = client.postgrest["life_goals"]
    private val milestoneTable get() = client.postgrest["goal_milestones"]

    suspend fun getGoalsByCoupleId(coupleId: String): List<LifeGoalDto> =
        goalTable.select {
            filter { eq("couple_id", coupleId) }
            filter { eq("is_deleted", false) }
            order("title", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertGoal(dto: LifeGoalDto): LifeGoalDto =
        goalTable.upsert(dto) { select() }.decodeSingle()

    suspend fun deleteGoal(id: String) {
        goalTable.update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
    }

    suspend fun getMilestonesByGoal(goalId: String): List<GoalMilestoneDto> =
        milestoneTable.select {
            filter { eq("goal_id", goalId) }
            filter { eq("is_deleted", false) }
            order("sort_order", Order.ASCENDING)
        }.decodeList()

    suspend fun upsertMilestone(dto: GoalMilestoneDto): GoalMilestoneDto =
        milestoneTable.upsert(dto) { select() }.decodeSingle()

    suspend fun deleteMilestone(id: String) {
        milestoneTable.update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
    }
}
