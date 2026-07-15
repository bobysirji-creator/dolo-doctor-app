package com.dolo.doctor.data

import com.dolo.doctor.data.model.*
import java.time.LocalDate

object DummyData {
    val profile = DoctorProfile(
        name = "Dr. Aisha Mehta",
        specialty = "General Physician",
        qualification = "MBBS, MD (Medicine)",
        registrationNumber = "DMC-45821",
        experienceYears = 11,
        consultationFee = 500,
        about = "Patient-focused physician providing clear guidance for everyday health concerns.",
        verified = true
    )

    val clinics = listOf(
        Clinic("clinic-1", "Care Point Clinic", "22 Green Park, New Delhi", "+91 11 4000 2200", "09:00 AM - 01:00 PM", "05:00 PM - 09:00 PM", 30, 12)
    )

    val appointments = listOf(
        Appointment("a1", 9, "Riya Sharma", "Self", "Morning", AppointmentStatus.IN_CONSULTATION, "08:42 AM", receiptNumber = "DL-DEMO-009"),
        Appointment("a2", 10, "Rahul Sharma", "Self", "Morning", AppointmentStatus.WAITING, "08:47 AM", receiptNumber = "DL-DEMO-010"),
        Appointment("a3", 11, "Maya Sharma", "Family member", "Morning", AppointmentStatus.ARRIVED, "08:50 AM", receiptNumber = "DL-DEMO-011"),
        Appointment("a4", 12, "Aman Gupta", "Self", "Morning", AppointmentStatus.BOOKED, "09:03 AM"),
        Appointment("a5", 13, "Nisha Verma", "Self", "Morning", AppointmentStatus.BOOKED, "09:10 AM"),
        Appointment("a6", 14, "Kabir Singh", "Family member", "Morning", AppointmentStatus.BOOKED, "09:16 AM")
    )

    val assistants = listOf(
        Assistant("staff-1", "Neha Kapoor", "9876543210", true, setOf(Permission.VIEW_QUEUE, Permission.UPDATE_QUEUE, Permission.CALL_NEXT_PATIENT, Permission.MARK_PATIENT_ARRIVED, Permission.VIEW_TODAY_APPOINTMENTS, Permission.BOOK_WALK_IN_APPOINTMENT, Permission.GENERATE_TOKEN_RECEIPT)),
        Assistant("staff-2", "Ravi Kumar", "9876501234", true, setOf(Permission.VIEW_QUEUE, Permission.VIEW_TODAY_APPOINTMENTS))
    )

    val announcements = listOf(
        Announcement("n1", "Sunday health camp", "Free blood-pressure and diabetes screening this Sunday.", AnnouncementType.CAMP, "20 Jul 2026", "20 Jul 2026", true),
        Announcement("n2", "Evening session unavailable", "The evening session will remain closed on 22 July.", AnnouncementType.AVAILABILITY, "22 Jul 2026", "22 Jul 2026", true),
        Announcement("n3", "Monsoon wellness consultation", "Preventive health consultation available through July.", AnnouncementType.OFFER, "01 Jul 2026", "31 Jul 2026", true)
    )

    val availabilityBlocks = listOf(
        AvailabilityBlock("b1", "clinic-1", "22 Jul 2026", "22 Jul 2026", "Evening", "Medical conference", false)
    )

    fun initialState(queueDate: String = LocalDate.now().toString()) = DoctorUiState(
        profile = profile,
        clinics = clinics,
        appointments = appointments,
        assistants = assistants,
        announcements = announcements,
        availabilityBlocks = availabilityBlocks,
        queueDate = queueDate,
        queueState = QueueState.ACTIVE,
        currentToken = 9
    )
}
