package com.dolo.doctor.integrations

import com.dolo.doctor.data.model.BookingSource
import com.dolo.doctor.data.model.Clinic
import java.time.LocalDate

data class BookingPolicyDecision(
    val allowed: Boolean,
    val message: String,
    val latestAllowedDate: String
)

object BookingPolicyEvaluator {
    fun evaluate(
        clinic: Clinic,
        clinicDate: String,
        requestedDate: String,
        source: BookingSource
    ): BookingPolicyDecision {
        val today = runCatching { LocalDate.parse(clinicDate) }.getOrNull()
            ?: return BookingPolicyDecision(false, "Clinic date is invalid.", clinicDate)
        val requested = runCatching { LocalDate.parse(requestedDate) }.getOrNull()
            ?: return BookingPolicyDecision(false, "Appointment date is invalid.", clinicDate)
        if (requested.isBefore(today)) {
            return BookingPolicyDecision(false, "Past appointments cannot be booked.", today.toString())
        }
        if (source == BookingSource.CLINIC_WALK_IN) {
            return if (requested == today) {
                BookingPolicyDecision(true, "Clinic walk-in booking is available for today.", today.toString())
            } else {
                BookingPolicyDecision(
                    false,
                    "Clinic walk-in booking is available only for the current day.",
                    today.toString()
                )
            }
        }
        if (requested == today) {
            return BookingPolicyDecision(true, "Current-day Patient App booking is available.", today.toString())
        }
        if (!clinic.futureBookingEnabled) {
            return BookingPolicyDecision(
                false,
                "This doctor accepts Patient App appointments only for the current day.",
                today.toString()
            )
        }
        val latest = today.plusDays(clinic.advanceBookingDays.toLong())
        return if (requested.isAfter(latest)) {
            BookingPolicyDecision(
                false,
                "Future appointments are available up to " + clinic.advanceBookingDays + " days ahead.",
                latest.toString()
            )
        } else {
            BookingPolicyDecision(
                true,
                "Future Patient App appointment is within the allowed window.",
                latest.toString()
            )
        }
    }
}