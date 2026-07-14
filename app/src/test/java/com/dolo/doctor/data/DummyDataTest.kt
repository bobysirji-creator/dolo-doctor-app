package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DummyDataTest {
    @Test fun initialQueueIsConsistent() {
        val state = DummyData.initialState()
        assertEquals(9, state.currentToken)
        assertEquals(QueueState.ACTIVE, state.queueState)
        assertEquals(AppointmentStatus.IN_CONSULTATION, state.appointments.single { it.token == 9 }.status)
        assertEquals(state.appointments.size, state.appointments.map { it.token }.distinct().size)
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