package com.dolo.doctor.ui.navigation

import com.dolo.doctor.data.model.Permission
import com.dolo.doctor.data.model.UserRole

enum class DoctorWorkspace(val label: String) {
    TODAY("Today"),
    APPOINTMENTS("Appointments"),
    CLINIC("Clinic"),
    MORE("More")
}

enum class DoctorMoreGroup(val label: String) {
    ACCOUNT("Account"),
    CLINIC_MANAGEMENT("Clinic management"),
    INSIGHTS_DATA("Insights and data")
}

enum class DoctorMoreDestination(val group: DoctorMoreGroup, val label: String, val description: String) {
    NOTIFICATIONS(DoctorMoreGroup.ACCOUNT, "Notifications", "Queue and hosted activity alerts"),
    PROFILE(DoctorMoreGroup.ACCOUNT, "Doctor profile", "Professional information and verification"),
    CHANGE_PIN(DoctorMoreGroup.ACCOUNT, "Change login PIN", "Protect this device login"),
    AVAILABILITY(DoctorMoreGroup.CLINIC_MANAGEMENT, "Availability", "Date blocks and appointment availability"),
    ANNOUNCEMENTS(DoctorMoreGroup.CLINIC_MANAGEMENT, "Announcements", "Clinic updates shown on the Doctor profile"),
    ASSISTANTS(DoctorMoreGroup.CLINIC_MANAGEMENT, "Assistants", "Accounts, access and permissions"),
    REPORTS(DoctorMoreGroup.INSIGHTS_DATA, "Reports", "Operational summary and Patient feedback"),
    HISTORY(DoctorMoreGroup.INSIGHTS_DATA, "Queue history", "Archived session and Patient history"),
    ACTIVITY(DoctorMoreGroup.INSIGHTS_DATA, "Activity log", "Audited queue activity"),
    HOSTED_SYNC(DoctorMoreGroup.INSIGHTS_DATA, "Hosted staff workspace", "Authoritative prototype clinic data"),
    SYNC(DoctorMoreGroup.INSIGHTS_DATA, "Local sync center", "Shared Patient App prototype bridge"),
    BACKUP(DoctorMoreGroup.INSIGHTS_DATA, "Backup and recovery", "Encrypted portable clinic backup")
}

object DoctorNavigationPolicy {
    fun canOpenClinic(role: UserRole?, permissions: Set<Permission>): Boolean =
        role == UserRole.DOCTOR || Permission.VIEW_CLINIC in permissions || Permission.MANAGE_CLINIC_AVAILABILITY in permissions

    fun visibleMoreDestinations(
        role: UserRole?,
        permissions: Set<Permission>,
        activeAssistantId: String?
    ): List<DoctorMoreDestination> {
        if (role == null) return emptyList()
        if (role == UserRole.DOCTOR) return DoctorMoreDestination.entries
        val result = linkedSetOf(
            DoctorMoreDestination.NOTIFICATIONS,
            DoctorMoreDestination.CHANGE_PIN
        )
        if (Permission.MANAGE_ANNOUNCEMENTS in permissions) result += DoctorMoreDestination.ANNOUNCEMENTS
        if (
            Permission.VIEW_REPORTS in permissions ||
            Permission.VIEW_PATIENT_FEEDBACK in permissions ||
            Permission.SEND_QUEUE_DELAY_NOTICE in permissions
        ) result += DoctorMoreDestination.REPORTS
        if (activeAssistantId == "staff-1") result += DoctorMoreDestination.HOSTED_SYNC
        return DoctorMoreDestination.entries.filter(result::contains)
    }
}