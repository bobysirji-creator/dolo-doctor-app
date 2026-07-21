package com.dolo.doctor.hosted

import com.dolo.doctor.data.model.UserRole
import org.json.JSONObject

data class HostedDoctorProfile(
    val doctorUserId: String,
    val displayName: String,
    val registrationNumber: String,
    val specialty: String,
    val qualification: String,
    val experienceYears: Int,
    val about: String,
    val verificationStatus: String,
    val profileRevision: String
)

data class HostedDoctorProfileRevision(
    val id: String,
    val doctorUserId: String,
    val doctorName: String,
    val displayName: String,
    val registrationNumber: String,
    val specialty: String,
    val qualification: String,
    val experienceYears: Int,
    val about: String,
    val status: String,
    val reviewNote: String,
    val submittedAt: String,
    val reviewedAt: String?
)

data class HostedDoctorProfileWorkspace(
    val profile: HostedDoctorProfile,
    val pendingRevision: HostedDoctorProfileRevision?
)

object HostedDoctorProfileJson {
    fun parseWorkspace(json: String): HostedDoctorProfileWorkspace {
        val root = JSONObject(json)
        require(root.optBoolean("authoritative"))
        return HostedDoctorProfileWorkspace(
            profile = parseProfile(root.getJSONObject("profile")),
            pendingRevision = root.optJSONObject("pendingRevision")?.let(::parseRevision)
        )
    }

    fun submissionBody(profile: HostedDoctorProfile): String = JSONObject()
        .put("displayName", profile.displayName.trim())
        .put("registrationNumber", profile.registrationNumber.trim())
        .put("specialty", profile.specialty.trim())
        .put("qualification", profile.qualification.trim())
        .put("experienceYears", profile.experienceYears)
        .put("about", profile.about.trim())
        .toString()

    private fun parseProfile(item: JSONObject) = HostedDoctorProfile(
        doctorUserId = item.getString("doctorUserId"),
        displayName = item.getString("displayName"),
        registrationNumber = item.getString("registrationNumber"),
        specialty = item.getString("specialty"),
        qualification = item.getString("qualification"),
        experienceYears = item.getInt("experienceYears"),
        about = item.optString("about"),
        verificationStatus = item.getString("verificationStatus"),
        profileRevision = item.getString("profileRevision")
    )

    private fun parseRevision(item: JSONObject) = HostedDoctorProfileRevision(
        id = item.getString("id"),
        doctorUserId = item.getString("doctorUserId"),
        doctorName = item.getString("doctorName"),
        displayName = item.getString("displayName"),
        registrationNumber = item.getString("registrationNumber"),
        specialty = item.getString("specialty"),
        qualification = item.getString("qualification"),
        experienceYears = item.getInt("experienceYears"),
        about = item.optString("about"),
        status = item.getString("status"),
        reviewNote = item.optString("reviewNote"),
        submittedAt = item.getString("submittedAt"),
        reviewedAt = item.optString("reviewedAt").takeIf { it.isNotBlank() }
    )
}
object HostedRoleBoundary {
    fun allows(localRole: UserRole, hostedRole: HostedStaffRole): Boolean =
        (localRole == UserRole.DOCTOR && hostedRole == HostedStaffRole.DOCTOR) ||
            (localRole == UserRole.ASSISTANT && hostedRole == HostedStaffRole.ASSISTANT)

    fun expected(localRole: UserRole): HostedStaffRole = when (localRole) {
        UserRole.DOCTOR -> HostedStaffRole.DOCTOR
        UserRole.ASSISTANT -> HostedStaffRole.ASSISTANT
    }
}