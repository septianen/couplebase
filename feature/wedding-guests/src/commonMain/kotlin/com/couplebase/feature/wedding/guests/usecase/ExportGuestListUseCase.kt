package com.couplebase.feature.wedding.guests.usecase

import com.couplebase.core.model.Guest

class ExportGuestListUseCase {

    operator fun invoke(guests: List<Guest>): String {
        val header = "Name,Email,Phone,RSVP Status,Meal Preference,Table,Plus One,Notes"
        val rows = guests.map { guest ->
            listOf(
                guest.name.escapeCsv(),
                (guest.email ?: "").escapeCsv(),
                (guest.phone ?: "").escapeCsv(),
                guest.rsvpStatus.name,
                (guest.mealPreference ?: "").escapeCsv(),
                (guest.tableNumber?.toString() ?: ""),
                if (guest.hasPlusOne) "Yes" else "No",
                (guest.notes ?: "").escapeCsv(),
            ).joinToString(",")
        }
        return (listOf(header) + rows).joinToString("\n")
    }
}

private fun String.escapeCsv(): String =
    if (contains(",") || contains("\"") || contains("\n")) {
        "\"${replace("\"", "\"\"")}\""
    } else this
