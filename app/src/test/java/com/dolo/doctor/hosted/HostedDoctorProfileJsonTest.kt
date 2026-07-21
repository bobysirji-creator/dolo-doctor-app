package com.dolo.doctor.hosted

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HostedDoctorProfileJsonTest {
    @Test
    fun parsesApprovedProfileAndPendingRevision() {
        val workspace = HostedDoctorProfileJson.parseWorkspace(
            """{"authoritative":true,"profile":{"doctorUserId":"doctor-1","displayName":"Dr Approved","registrationNumber":"REG-1","specialty":"General Medicine","qualification":"MBBS","experienceYears":10,"about":"Approved profile","verificationStatus":"VERIFIED","profileRevision":"4"},"pendingRevision":{"id":"revision-1","doctorUserId":"doctor-1","doctorName":"Dr Approved","displayName":"Dr Proposed","registrationNumber":"REG-1","specialty":"Family Medicine","qualification":"MBBS, MD","experienceYears":11,"about":"Proposed profile","status":"PENDING","reviewNote":"","submittedAt":"2026-07-21T12:00:00Z","reviewedAt":null}}"""
        )
        assertEquals("Dr Approved", workspace.profile.displayName)
        assertEquals("Family Medicine", workspace.pendingRevision?.specialty)
        assertEquals("PENDING", workspace.pendingRevision?.status)
        assertNull(workspace.pendingRevision?.reviewedAt)
    }

    @Test
    fun submissionBodyContainsOnlyBoundedEditableFields() {
        val profile = HostedDoctorProfile("doctor-1", " Dr Test ", " REG-1 ", " General Medicine ", " MBBS ", 12, " About ", "VERIFIED", "7")
        val body = org.json.JSONObject(HostedDoctorProfileJson.submissionBody(profile))
        assertEquals(setOf("displayName", "registrationNumber", "specialty", "qualification", "experienceYears", "about"), body.keys().asSequence().toSet())
        assertEquals("Dr Test", body.getString("displayName"))
        assertEquals(12, body.getInt("experienceYears"))
    }
}