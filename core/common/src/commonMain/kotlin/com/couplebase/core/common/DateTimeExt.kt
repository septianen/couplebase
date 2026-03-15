package com.couplebase.core.common

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Returns the current Instant.
 */
fun now(): Instant = Clock.System.now()

/**
 * Returns the current LocalDateTime in the system's default timezone.
 */
fun nowLocal(): LocalDateTime = now().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Returns today's date in the system's default timezone.
 */
fun today(): LocalDate = nowLocal().date

/**
 * Returns the number of days from today until the given date.
 * Positive if the date is in the future, negative if in the past.
 */
fun LocalDate.daysFromToday(): Int = today().daysUntil(this)

/**
 * Formats an Instant to an ISO 8601 string for storage/sync.
 */
fun Instant.toIsoString(): String = this.toString()

/**
 * Parses an ISO 8601 string to an Instant.
 */
fun String.toInstant(): Instant = Instant.parse(this)

/**
 * Formats a LocalDate as "MMM dd, yyyy" style (e.g. "Jun 15, 2026").
 */
fun LocalDate.toDisplayString(): String {
    val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${months[monthNumber - 1]} $dayOfMonth, $year"
}
