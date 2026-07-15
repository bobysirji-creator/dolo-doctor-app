package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyDataTest {
    @Test fun initialQueueIsConsistent() {
        val state = DummyData.initialState()
        assertEquals(1, state.currentToken)
        assertEquals(QueueState.ACTIVE, state.queueState)
        assertEquals(AppointmentStatus.IN_CONSULTATION, state.appointments.single { it.token == 1 }.status)
        assertTrue(state.appointments.groupBy { it.session }.values.all { session -> session.map { it.token }.distinct().size == session.size })
    }

    @Test fun announcementsAndAvailabilityArePatientReady() {
        assertTrue(DummyData.announcements.any { it.type == AnnouncementType.CAMP })
        assertTrue(DummyData.announcements.any { it.type == AnnouncementType.AVAILABILITY })
        assertFalse(DummyData.availabilityBlocks.first().appointmentsEnabled)
    }

    @Test fun assistantsUseIndividualPermissionSets() {
        assertEquals(2, DummyData.assistants.size)
        assertTrue(DummyData.assistants.all { it.permissions.contains(Permission.VIEW_QUEUE) })
        assertTrue(DummyData.assistants.map { it.phone }.distinct().size == DummyData.assistants.size)
    }
}
