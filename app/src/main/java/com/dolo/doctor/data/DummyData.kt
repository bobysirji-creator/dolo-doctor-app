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
        Appointment("a1", 1, "Riya Sharma", "Self", "Morning", AppointmentStatus.IN_CONSULTATION, "08:42 AM", queueOrder = 1, receiptNumber = "DL-DEMO-M-001", consultationFee = 500, paymentStatus = PaymentStatus.PAID, paymentMethod = PaymentMethod.CASH, paidAt = "08:40 AM"),
        Appointment("a2", 2, "Rahul Sharma", "Self", "Morning", AppointmentStatus.WAITING, "08:47 AM", queueOrder = 2, receiptNumber = "DL-DEMO-M-002", consultationFee = 500, paymentStatus = PaymentStatus.PAID, paymentMethod = PaymentMethod.UPI, paidAt = "08:45 AM"),
        Appointment("a3", 3, "Maya Sharma", "Family member", "Morning", AppointmentStatus.ARRIVED, "08:50 AM", queueOrder = 3, receiptNumber = "DL-DEMO-M-003", consultationFee = 500, paymentStatus = PaymentStatus.PAID, paymentMethod = PaymentMethod.CARD, paidAt = "08:49 AM"),
        Appointment("a4", 4, "Aman Gupta", "Self", "Morning", AppointmentStatus.BOOKED, "09:03 AM", queueOrder = 0),
        Appointment("a5", 5, "Nisha Verma", "Self", "Morning", AppointmentStatus.BOOKED, "09:10 AM", queueOrder = 0),
        Appointment("a6", 6, "Kabir Singh", "Family member", "Morning", AppointmentStatus.BOOKED, "09:16 AM", queueOrder = 0)
    )

    val assistants = listOf(
        Assistant("staff-1", "Neha Kapoor", "9876543210", true, setOf(Permission.VIEW_QUEUE, Permission.UPDATE_QUEUE, Permission.CALL_NEXT_PATIENT, Permission.MARK_PATIENT_ARRIVED, Permission.VIEW_TODAY_APPOINTMENTS, Permission.BOOK_WALK_IN_APPOINTMENT, Permission.GENERATE_TOKEN_RECEIPT, Permission.CONFIRM_CONSULTATION_FEE)),
        Assistant("staff-2", "Ravi Kumar", "9876501234", true, setOf(Permission.VIEW_QUEUE, Permission.VIEW_TODAY_APPOINTMENTS))
    )

    val announcements = listOf(
        Announcement("n1", "Sunday health camp", "Free blood-pressure and diabetes screening this Sunday.", AnnouncementType.CAMP, "2026-07-20", "2026-07-20", true),
        Announcement("n2", "Evening session unavailable", "The evening session will remain closed on 22 July.", AnnouncementType.AVAILABILITY, "2026-07-22", "2026-07-22", true),
        Announcement("n3", "Monsoon wellness consultation", "Preventive health consultation available through July.", AnnouncementType.OFFER, "2026-07-01", "2026-07-31", true)
    )

    val availabilityBlocks = listOf(
        AvailabilityBlock("b1", "clinic-1", "2026-07-22", "2026-07-22", "Evening", "Medical conference", false)
    )

    fun initialState(queueDate: String = LocalDate.now().toString()) = DoctorUiState(
        profile = profile,
        clinics = clinics,
        appointments = appointments,
        assistants = assistants,
        announcements = announcements,
        availabilityBlocks = availabilityBlocks,
        queueDate = queueDate,
        sessionQueues = listOf(
            ConsultationQueue("Morning", QueueState.ACTIVE, 1),
            ConsultationQueue("Evening", QueueState.NOT_STARTED, 0)
        ),
        queueState = QueueState.ACTIVE,
        currentToken = 1
    )
}
