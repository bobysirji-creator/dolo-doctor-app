package com.dolo.doctor.integrations

import com.dolo.doctor.data.model.BookingSource
import com.dolo.doctor.data.model.Clinic
import com.dolo.doctor.data.model.isSessionClosed
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
        source: BookingSource,
        session: String? = null
    ): BookingPolicyDecision {
        val today = runCatching { LocalDate.parse(clinicDate) }.getOrNull()
            ?: return BookingPolicyDecision(false, "Clinic date is invalid.", clinicDate)
        val requested = runCatching { LocalDate.parse(requestedDate) }.getOrNull()
            ?: return BookingPolicyDecision(false, "Appointment date is invalid.", clinicDate)
        if (requested.isBefore(today)) {
            return BookingPolicyDecision(false, "Past appointments cannot be booked.", today.toString())
        }

        val latest = when {
            source == BookingSource.CLINIC_WALK_IN && requested != today -> {
                return BookingPolicyDecision(
                    false,
                    "Clinic walk-in booking is available only for the current day.",
                    today.toString()
                )
            }
            source == BookingSource.PATIENT_APP && requested != today && !clinic.futureBookingEnabled -> {
                return BookingPolicyDecision(
                    false,
                    "This doctor accepts Patient App appointments only for the current day.",
                    today.toString()
                )
            }
            source == BookingSource.PATIENT_APP && requested != today -> {
                val limit = today.plusDays(clinic.advanceBookingDays.toLong())
                if (requested.isAfter(limit)) {
                    return BookingPolicyDecision(
                        false,
                        "Future appointments are available up to " + clinic.advanceBookingDays + " days ahead.",
                        limit.toString()
                    )
                }
                limit
            }
            else -> today
        }

        if (session != null) {
            if (session !in setOf("Morning", "Evening")) {
                return BookingPolicyDecision(false, "Select Morning or Evening.", latest.toString())
            }
            if (clinic.isSessionClosed(requested, session)) {
                return BookingPolicyDecision(
                    false,
                    session + " appointments are closed every " + requested.dayOfWeek.name.lowercase()
                        .replaceFirstChar(Char::uppercase) + ".",
                    latest.toString()
                )
            }
        }

        val message = when {
            source == BookingSource.CLINIC_WALK_IN -> "Clinic walk-in booking is available for today."
            requested == today -> "Current-day Patient App booking is available."
            else -> "Future Patient App appointment is within the allowed window."
        }
        return BookingPolicyDecision(true, message, latest.toString())
    }
}