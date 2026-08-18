package com.dolo.doctor.hosted

import org.json.JSONArray
import org.json.JSONObject

enum class HostedDoctorOnboardingStatus { PENDING, APPROVED, REJECTED, SUPERSEDED }

data class HostedDoctorOnboardingDraft(
    val displayName: String = "",
    val registrationNumber: String = "",
    val specialty: String = "",
    val qualification: String = "",
    val experienceYears: Int = 0,
    val about: String = "",
    val clinicName: String = "",
    val addressLine: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val clinicPhoneE164: String = "+91",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val consultationFeeMinor: Int = 0,
    val futureBookingDays: Int = 0,
    val rescheduleWindowDays: Int = 10,
    val weeklySessions: List<HostedWeeklySession> = defaultPilotWeeklySessions()
)

data class HostedDoctorOnboarding(
    val id: String,
    val draft: HostedDoctorOnboardingDraft,
    val status: HostedDoctorOnboardingStatus,
    val reviewNote: String,
    val createdClinicId: String?,
    val submittedAt: String,
    val reviewedAt: String?
)

data class HostedDoctorOnboardingWorkspace(
    val onboarding: HostedDoctorOnboarding?,
    val workspaceReady: Boolean
)

data class HostedDoctorOnboardingUiState(
    val loading: Boolean = false,
    val workspace: HostedDoctorOnboardingWorkspace? = null,
    val message: String = "Complete your Doctor and clinic setup for Admin review.",
    val error: Boolean = false
)

fun defaultPilotWeeklySessions(): List<HostedWeeklySession> = buildList {
    for (day in 1..6) {
        add(HostedWeeklySession(day, "MORNING", "09:00", "13:00", 30, 12, true))
        add(HostedWeeklySession(day, "EVENING", "17:00", "21:00", 30, 12, true))
    }
}

object HostedDoctorOnboardingJson {
    fun parseWorkspace(json: String): HostedDoctorOnboardingWorkspace {
        val root = JSONObject(json)
        require(root.optBoolean("authoritative"))
        val item = if (root.isNull("onboarding")) null else parse(root.getJSONObject("onboarding"))
        return HostedDoctorOnboardingWorkspace(item, root.optBoolean("workspaceReady"))
    }

    fun body(draft: HostedDoctorOnboardingDraft): String = JSONObject()
        .put("displayName", draft.displayName.trim())
        .put("registrationNumber", draft.registrationNumber.trim())
        .put("specialty", draft.specialty.trim())
        .put("qualification", draft.qualification.trim())
        .put("experienceYears", draft.experienceYears)
        .put("about", draft.about.trim())
        .put("clinicName", draft.clinicName.trim())
        .put("addressLine", draft.addressLine.trim())
        .put("city", draft.city.trim())
        .put("state", draft.state.trim())
        .put("pincode", draft.pincode.trim())
        .put("clinicPhoneE164", draft.clinicPhoneE164.trim())
        .put("latitude", draft.latitude ?: JSONObject.NULL)
        .put("longitude", draft.longitude ?: JSONObject.NULL)
        .put("consultationFeeMinor", draft.consultationFeeMinor)
        .put("futureBookingDays", draft.futureBookingDays)
        .put("rescheduleWindowDays", draft.rescheduleWindowDays)
        .put("weeklySessions", JSONArray().apply {
            draft.weeklySessions.forEach { row ->
                put(JSONObject()
                    .put("dayOfWeek", row.dayOfWeek)
                    .put("session", row.session)
                    .put("startsAt", row.startsAt)
                    .put("endsAt", row.endsAt)
                    .put("maxTokens", row.maxTokens)
                    .put("averageConsultationMinutes", row.averageConsultationMinutes)
                    .put("bookingEnabled", row.bookingEnabled))
            }
        }).toString()

    private fun parse(item: JSONObject): HostedDoctorOnboarding {
        val weekly = item.getJSONArray("weeklySessions")
        return HostedDoctorOnboarding(
            id = item.getString("id"),
            draft = HostedDoctorOnboardingDraft(
                displayName = item.getString("displayName"),
                registrationNumber = item.getString("registrationNumber"),
                specialty = item.getString("specialty"),
                qualification = item.getString("qualification"),
                experienceYears = item.getInt("experienceYears"),
                about = item.optString("about"),
                clinicName = item.getString("clinicName"),
                addressLine = item.getString("addressLine"),
                city = item.getString("city"),
                state = item.getString("state"),
                pincode = item.getString("pincode"),
                clinicPhoneE164 = item.getString("clinicPhoneE164"),
                latitude = item.optDouble("latitude").takeUnless { item.isNull("latitude") },
                longitude = item.optDouble("longitude").takeUnless { item.isNull("longitude") },
                consultationFeeMinor = item.getInt("consultationFeeMinor"),
                futureBookingDays = item.getInt("futureBookingDays"),
                rescheduleWindowDays = item.getInt("rescheduleWindowDays"),
                weeklySessions = buildList {
                    for (index in 0 until weekly.length()) {
                        val row = weekly.getJSONObject(index)
                        add(HostedWeeklySession(row.getInt("dayOfWeek"), row.getString("session"), row.getString("startsAt").take(5), row.getString("endsAt").take(5), row.getInt("maxTokens"), row.getInt("averageConsultationMinutes"), row.getBoolean("bookingEnabled")))
                    }
                }
            ),
            status = HostedDoctorOnboardingStatus.valueOf(item.getString("status")),
            reviewNote = item.optString("reviewNote"),
            createdClinicId = item.optString("createdClinicId").takeIf { it.isNotBlank() },
            submittedAt = item.getString("submittedAt"),
            reviewedAt = item.optString("reviewedAt").takeIf { it.isNotBlank() }
        )
    }
}