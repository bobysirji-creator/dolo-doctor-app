package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HostedPatientReviewJsonTest {
    @Test
    fun parsesPublishedDoctorOwnedReviews() {
        val reviews = HostedPatientReviewJson.parse(
            """{"reviews":[{"id":"70000000-0000-4000-8000-000000000025","appointmentId":"50000000-0000-4000-8000-000000000025","patientName":"Patient Demo","clinicName":"DO-LO Clinic","rating":5,"comment":"Kind consultation.","status":"PUBLISHED","submittedAt":"2026-07-22T08:00:00.000Z"}]}"""
        )
        assertEquals(1, reviews.size)
        assertEquals(5, reviews.single().rating)
        assertEquals("Patient Demo", reviews.single().patientName)
    }

    @Test
    fun refusesUnpublishedReviewPayloads() {
        assertThrows(IllegalArgumentException::class.java) {
            HostedPatientReviewJson.parse(
                """{"reviews":[{"id":"x","appointmentId":"y","patientName":"Patient","clinicName":"Clinic","rating":4,"comment":"","status":"PENDING","submittedAt":"2026-07-22T08:00:00.000Z"}]}"""
            )
        }
    }
}
