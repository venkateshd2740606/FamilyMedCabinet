package com.familymedcabinet.engine

import com.familymedcabinet.domain.model.Medicine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CabinetEngine @Inject constructor() {

    private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun formatExpiry(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFmt)

    fun parseExpiryDate(input: String): Long? = runCatching {
        LocalDate.parse(input.trim()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }.getOrNull()

    fun expiryMillisInDays(days: Int): Long =
        LocalDate.now().plusDays(days.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun daysUntilExpiry(medicine: Medicine): Long {
        val expiry = Instant.ofEpochMilli(medicine.expiryDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(LocalDate.now(), expiry)
    }

    fun isExpiringSoon(medicine: Medicine, withinDays: Int = 30): Boolean =
        !medicine.finished && daysUntilExpiry(medicine) in 0..withinDays.toLong()

    fun isExpired(medicine: Medicine): Boolean =
        !medicine.finished && daysUntilExpiry(medicine) < 0
}
