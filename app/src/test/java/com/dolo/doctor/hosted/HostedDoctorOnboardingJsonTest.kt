package com.dolo.doctor.hosted

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HostedDoctorOnboardingJsonTest {
    @Test fun parsesPendingWorkspaceAndBuildsSubmission() {
        val workspace=HostedDoctorOnboardingJson.parseWorkspace("""{"authoritative":true,"workspaceReady":false,"onboarding":{"id":"10000000-0000-4000-8000-000000000001","displayName":"Dr Pilot","registrationNumber":"REG-1","specialty":"General Medicine","qualification":"MBBS","experienceYears":6,"about":"Care","clinicName":"Pilot Clinic","addressLine":"1 Main Road","city":"Mumbai","state":"Maharashtra","pincode":"400001","clinicPhoneE164":"+919876543210","latitude":null,"longitude":null,"consultationFeeMinor":50000,"futureBookingDays":7,"rescheduleWindowDays":10,"weeklySessions":[{"dayOfWeek":1,"session":"MORNING","startsAt":"09:00:00","endsAt":"13:00:00","maxTokens":30,"averageConsultationMinutes":12,"bookingEnabled":true}],"status":"PENDING","reviewNote":"","createdClinicId":null,"submittedAt":"2026-08-18T10:00:00Z","reviewedAt":null}}""")
        assertFalse(workspace.workspaceReady)
        assertEquals(HostedDoctorOnboardingStatus.PENDING,workspace.onboarding?.status)
        assertEquals("09:00",workspace.onboarding?.draft?.weeklySessions?.single()?.startsAt)
        val body=JSONObject(HostedDoctorOnboardingJson.body(workspace.onboarding!!.draft))
        assertEquals("Pilot Clinic",body.getString("clinicName"))
        assertEquals(1,body.getJSONArray("weeklySessions").length())
    }
}