package com.couplebase.feature.wedding.guests.usecase

import com.couplebase.core.common.Result
import com.couplebase.core.common.generateUuid
import com.couplebase.core.domain.repository.GuestRepository
import com.couplebase.core.model.Guest
import com.couplebase.core.model.RsvpStatus

class ImportGuestsUseCase(
    private val repository: GuestRepository,
) {
    suspend operator fun invoke(coupleId: String, csvContent: String): Result<Int> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return Result.Success(0)

        var imported = 0
        // Skip header row
        for (line in lines.drop(1)) {
            val fields = parseCsvLine(line)
            if (fields.isEmpty()) continue
            val name = fields.getOrNull(0)?.trim() ?: continue
            if (name.isBlank()) continue

            val guest = Guest(
                id = generateUuid(),
                coupleId = coupleId,
                name = name,
                email = fields.getOrNull(1)?.trim()?.ifBlank { null },
                phone = fields.getOrNull(2)?.trim()?.ifBlank { null },
                rsvpStatus = fields.getOrNull(3)?.trim()?.let {
                    runCatching { RsvpStatus.valueOf(it) }.getOrNull()
                } ?: RsvpStatus.PENDING,
                mealPreference = fields.getOrNull(4)?.trim()?.ifBlank { null },
                tableNumber = fields.getOrNull(5)?.trim()?.toIntOrNull(),
                hasPlusOne = fields.getOrNull(6)?.trim()?.equals("Yes", ignoreCase = true) == true,
                notes = fields.getOrNull(7)?.trim()?.ifBlank { null },
                updatedAt = "",
            )
            repository.upsert(guest)
            imported++
        }
        return Result.Success(imported)
    }
}

private fun parseCsvLine(line: String): List<String> {
    val fields = mutableListOf<String>()
    val current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' && !inQuotes -> inQuotes = true
            c == '"' && inQuotes -> {
                if (i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = false
                }
            }
            c == ',' && !inQuotes -> {
                fields.add(current.toString())
                current.clear()
            }
            else -> current.append(c)
        }
        i++
    }
    fields.add(current.toString())
    return fields
}
