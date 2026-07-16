package com.dolo.doctor.integrations

import com.dolo.doctor.data.DummyData
import com.dolo.doctor.data.model.BookingSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingPolicyTest {
    private val clinic = DummyData.clinics.first()

    @Test fun currentDayOnlyDoctorRejectsFuturePatientAppBooking() {
        val decision = BookingPolicyEvaluator.evaluate(
            clinic.copy(futureBookingEnabled = false),
            "2026-07-16",
            "2026-07-17",
            BookingSource.PATIENT_APP
        )

        assertFalse(decision.allowed)
        assertEquals("2026-07-16", decision.latestAllowedDate)
        assertTrue(decision.message.contains("current day"))
    }

    @Test fun enabledFutureWindowIncludesLimitAndRejectsBeyondIt() {
        val enabled = clinic.copy(futureBookingEnabled = true, advanceBookingDays = 10)

        val within = BookingPolicyEvaluator.evaluate(
            enabled, "2026-07-16", "2026-07-26", BookingSource.PATIENT_APP
        )
        val beyond = BookingPolicyEvaluator.evaluate(
            enabled, "2026-07-16", "2026-07-27", BookingSource.PATIENT_APP
        )

        assertTrue(within.allowed)
        assertEquals("2026-07-26", within.latestAllowedDate)
        assertFalse(beyond.allowed)
        assertEquals("2026-07-26", beyond.latestAllowedDate)
    }

    @Test fun clinicWalkInRemainsCurrentDayOnlyEvenWhenFutureEnabled() {
        val enabled = clinic.copy(futureBookingEnabled = true, advanceBookingDays = 30)

        val today = BookingPolicyEvaluator.evaluate(
            enabled, "2026-07-16", "2026-07-16", BookingSource.CLINIC_WALK_IN
        )
        val future = BookingPolicyEvaluator.evaluate(
            enabled, "2026-07-16", "2026-07-17", BookingSource.CLINIC_WALK_IN
        )

        assertTrue(today.allowed)
        assertFalse(future.allowed)
        assertTrue(future.message.contains("current day"))
    }

    @Test fun pastAndMalformedDatesAreRejected() {
        assertFalse(
            BookingPolicyEvaluator.evaluate(
                clinic, "2026-07-16", "2026-07-15", BookingSource.PATIENT_APP
            ).allowed
        )
        assertFalse(
            BookingPolicyEvaluator.evaluate(
                clinic, "2026-07-16", "not-a-date", BookingSource.PATIENT_APP
            ).allowed
        )
    }
}