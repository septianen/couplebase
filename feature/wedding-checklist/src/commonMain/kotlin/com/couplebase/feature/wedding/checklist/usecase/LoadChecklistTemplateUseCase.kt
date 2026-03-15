package com.couplebase.feature.wedding.checklist.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.ChecklistRepository
import com.couplebase.core.model.AssignedTo
import com.couplebase.core.model.ChecklistItem

class LoadChecklistTemplateUseCase(
    private val repository: ChecklistRepository,
) {
    suspend operator fun invoke(coupleId: String): Result<Unit> {
        val existing = repository.getProgress(coupleId)
        if (existing is Result.Success && existing.data.second > 0) {
            return Result.Success(Unit)
        }

        val templateItems = createTemplateItems(coupleId)
        templateItems.forEach { item ->
            repository.upsert(item)
        }
        return Result.Success(Unit)
    }
}

private fun createTemplateItems(coupleId: String): List<ChecklistItem> {
    var order = 0

    fun item(
        title: String,
        category: String,
        assignedTo: AssignedTo = AssignedTo.BOTH,
        dueDate: String? = null,
    ) = ChecklistItem(
        id = generateUuid(),
        coupleId = coupleId,
        title = title,
        category = category,
        assignedTo = assignedTo,
        dueDate = dueDate,
        sortOrder = order++,
        updatedAt = "",
    )

    return listOf(
        // 12+ Months Before
        item("Set a wedding budget", "12+ Months Before"),
        item("Choose a wedding date", "12+ Months Before"),
        item("Research and visit venues", "12+ Months Before"),
        item("Start guest list draft", "12+ Months Before"),
        item("Research photographers", "12+ Months Before", AssignedTo.ME),
        item("Research videographers", "12+ Months Before", AssignedTo.ME),
        item("Hire a wedding planner", "12+ Months Before"),
        item("Decide on wedding style/theme", "12+ Months Before"),
        item("Start engagement party planning", "12+ Months Before"),
        item("Research wedding insurance", "12+ Months Before"),

        // 6-12 Months Before
        item("Book ceremony venue", "6-12 Months Before"),
        item("Book reception venue", "6-12 Months Before"),
        item("Book photographer", "6-12 Months Before", AssignedTo.ME),
        item("Book videographer", "6-12 Months Before", AssignedTo.ME),
        item("Book caterer or finalize menu", "6-12 Months Before", AssignedTo.PARTNER),
        item("Choose wedding party members", "6-12 Months Before"),
        item("Book florist", "6-12 Months Before", AssignedTo.ME),
        item("Book DJ or band", "6-12 Months Before", AssignedTo.PARTNER),
        item("Shop for wedding attire", "6-12 Months Before", AssignedTo.ME),
        item("Shop for wedding accessories", "6-12 Months Before", AssignedTo.ME),
        item("Book officiant", "6-12 Months Before"),
        item("Research honeymoon destinations", "6-12 Months Before"),

        // 3-6 Months Before
        item("Send save-the-dates", "3-6 Months Before"),
        item("Book honeymoon travel", "3-6 Months Before"),
        item("Order wedding invitations", "3-6 Months Before", AssignedTo.ME),
        item("Plan ceremony details and readings", "3-6 Months Before"),
        item("Register for wedding gifts", "3-6 Months Before"),
        item("Book hair and makeup artist", "3-6 Months Before", AssignedTo.ME),
        item("Order wedding cake", "3-6 Months Before", AssignedTo.PARTNER),
        item("Plan rehearsal dinner", "3-6 Months Before", AssignedTo.PARTNER),
        item("Book transportation", "3-6 Months Before"),
        item("Choose wedding rings", "3-6 Months Before"),

        // 1-3 Months Before
        item("Send invitations", "1-3 Months Before"),
        item("Finalize guest list and seating chart", "1-3 Months Before"),
        item("Apply for marriage license", "1-3 Months Before"),
        item("Write personal vows", "1-3 Months Before"),
        item("Schedule final dress/suit fitting", "1-3 Months Before", AssignedTo.ME),
        item("Plan bachelor/bachelorette party", "1-3 Months Before"),
        item("Confirm all vendor contracts", "1-3 Months Before"),
        item("Create wedding day timeline", "1-3 Months Before"),
        item("Arrange accommodation for out-of-town guests", "1-3 Months Before"),
        item("Buy gifts for wedding party", "1-3 Months Before"),

        // Final Month
        item("Confirm final guest count with caterer", "Final Month"),
        item("Finalize seating chart", "Final Month"),
        item("Prepare day-of emergency kit", "Final Month"),
        item("Break in wedding shoes", "Final Month", AssignedTo.ME),
        item("Confirm honeymoon reservations", "Final Month"),
        item("Pack for honeymoon", "Final Month"),
        item("Final venue walkthrough", "Final Month"),
        item("Distribute final timeline to wedding party", "Final Month"),
        item("Prepare vendor payments and tips", "Final Month"),
        item("Enjoy your wedding day!", "Final Month"),
    )
}
