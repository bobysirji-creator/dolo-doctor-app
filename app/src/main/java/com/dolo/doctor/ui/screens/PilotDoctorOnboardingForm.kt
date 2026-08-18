package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dolo.doctor.hosted.*
import com.dolo.doctor.ui.components.ElevatedSection

@Composable
fun PilotDoctorSetupContent(
    displayName: String,
    state: HostedDoctorOnboardingUiState,
    enabled: Boolean,
    onSubmit: (HostedDoctorOnboardingDraft) -> Unit,
    onRefresh: () -> Unit
) {
    val onboarding = state.workspace?.onboarding
    val locked = onboarding?.status == HostedDoctorOnboardingStatus.PENDING || onboarding?.status == HostedDoctorOnboardingStatus.APPROVED
    var draft by remember(onboarding?.id, displayName) { mutableStateOf(onboarding?.draft ?: HostedDoctorOnboardingDraft(displayName = displayName)) }
    var localError by remember { mutableStateOf<String?>(null) }
    var latitudeText by remember(onboarding?.id) { mutableStateOf(onboarding?.draft?.latitude?.toString().orEmpty()) }
    var longitudeText by remember(onboarding?.id) { mutableStateOf(onboarding?.draft?.longitude?.toString().orEmpty()) }

    ElevatedSection("Doctor and clinic onboarding") {
        Text(state.message, color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        onboarding?.let {
            AssistChip(onClick = {}, enabled = false, label = { Text(it.status.name) })
            if (it.status == HostedDoctorOnboardingStatus.REJECTED && it.reviewNote.isNotBlank()) {
                Text("Admin note: ${it.reviewNote}", color = MaterialTheme.colorScheme.error)
            }
        }
        if (!locked) {
            SetupTextField("Doctor name", draft.displayName) { draft = draft.copy(displayName = it) }
            SetupTextField("Medical registration number", draft.registrationNumber) { draft = draft.copy(registrationNumber = it) }
            SetupTextField("Specialty", draft.specialty) { draft = draft.copy(specialty = it) }
            SetupTextField("Qualification", draft.qualification) { draft = draft.copy(qualification = it) }
            SetupNumberField("Experience in years", draft.experienceYears.toString()) { draft = draft.copy(experienceYears = it.toIntOrNull() ?: 0) }
            SetupTextField("About Doctor", draft.about, singleLine = false) { draft = draft.copy(about = it) }
            HorizontalDivider()
            Text("Clinic details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SetupTextField("Clinic name", draft.clinicName) { draft = draft.copy(clinicName = it) }
            SetupTextField("Full address", draft.addressLine, singleLine = false) { draft = draft.copy(addressLine = it) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SetupTextField("City", draft.city) { draft = draft.copy(city = it) } }
                Box(Modifier.weight(1f)) { SetupTextField("State", draft.state) { draft = draft.copy(state = it) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SetupNumberField("PIN code", draft.pincode) { draft = draft.copy(pincode = it.take(6)) } }
                Box(Modifier.weight(1f)) { SetupNumberField("Clinic mobile", draft.clinicPhoneE164.removePrefix("+91")) { draft = draft.copy(clinicPhoneE164 = "+91" + it.take(10)) } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SetupNumberField("Clinic fee ₹", (draft.consultationFeeMinor / 100).toString()) { draft = draft.copy(consultationFeeMinor = (it.toIntOrNull() ?: 0) * 100) } }
                Box(Modifier.weight(1f)) { SetupNumberField("Future booking days", draft.futureBookingDays.toString()) { draft = draft.copy(futureBookingDays = it.toIntOrNull() ?: 0) } }
            }
            SetupNumberField("Missed appointment reschedule days", draft.rescheduleWindowDays.toString()) { draft = draft.copy(rescheduleWindowDays = it.toIntOrNull() ?: 10) }
            Text("Clinic coordinates (optional, but required for Nearby discovery)", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SetupDecimalField("Latitude", latitudeText) { latitudeText = it } }
                Box(Modifier.weight(1f)) { SetupDecimalField("Longitude", longitudeText) { longitudeText = it } }
            }
            HorizontalDivider()
            WeeklySetupEditor(draft.weeklySessions) { draft = draft.copy(weeklySessions = it) }
            localError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    val submitted = draft.copy(latitude = latitudeText.toDoubleOrNull(), longitude = longitudeText.toDoubleOrNull())
                    localError = validatePilotDraft(submitted, latitudeText, longitudeText)
                    if (localError == null) onSubmit(submitted)
                },
                enabled = enabled && !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (onboarding?.status == HostedDoctorOnboardingStatus.REJECTED) "Resubmit for Admin review" else "Submit for Admin review") }
        } else {
            Text(
                if (onboarding?.status == HostedDoctorOnboardingStatus.APPROVED) "Approved. Refresh to open the server-authoritative clinic workspace."
                else "Editing is locked while Admin reviews this submission.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedButton(onClick = onRefresh, enabled = enabled && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (state.loading) "Checking…" else "Refresh setup status") }
    }
}

@Composable private fun WeeklySetupEditor(sessions: List<HostedWeeklySession>, onChange: (List<HostedWeeklySession>) -> Unit) {
    val selected = sessions.map { it.dayOfWeek }.toSet()
    Text("Weekly clinic days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("Initial timings can be fine-tuned after approval.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(listOf(1, 2, 3, 4, 5, 6, 0)) { day ->
            val label = mapOf(0 to "Sun", 1 to "Mon", 2 to "Tue", 3 to "Wed", 4 to "Thu", 5 to "Fri", 6 to "Sat").getValue(day)
            FilterChip(selected = day in selected, onClick = {
                onChange(if (day in selected) sessions.filterNot { it.dayOfWeek == day } else sessions + listOf(
                    HostedWeeklySession(day, "MORNING", "09:00", "13:00", 30, 12, true),
                    HostedWeeklySession(day, "EVENING", "17:00", "21:00", 30, 12, true)
                ))
            }, label = { Text(label) })
        }
    }
    SessionDefaultsEditor("MORNING", sessions, onChange)
    SessionDefaultsEditor("EVENING", sessions, onChange)
}

@Composable private fun SessionDefaultsEditor(name: String, sessions: List<HostedWeeklySession>, onChange: (List<HostedWeeklySession>) -> Unit) {
    val first = sessions.firstOrNull { it.session == name } ?: return
    Text(if (name == "MORNING") "Morning session" else "Evening session", fontWeight = FontWeight.SemiBold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SetupTextField("Starts", first.startsAt) { value -> onChange(sessions.map { if (it.session == name) it.copy(startsAt = value.take(5)) else it }) } }
        Box(Modifier.weight(1f)) { SetupTextField("Ends", first.endsAt) { value -> onChange(sessions.map { if (it.session == name) it.copy(endsAt = value.take(5)) else it }) } }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SetupNumberField("Max tokens", first.maxTokens.toString()) { value -> onChange(sessions.map { if (it.session == name) it.copy(maxTokens = value.toIntOrNull() ?: 0) else it }) } }
        Box(Modifier.weight(1f)) { SetupNumberField("Average minutes", first.averageConsultationMinutes.toString()) { value -> onChange(sessions.map { if (it.session == name) it.copy(averageConsultationMinutes = value.toIntOrNull() ?: 0) else it }) } }
    }
}

@Composable private fun SetupTextField(label: String, value: String, singleLine: Boolean = true, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValue, label = { Text(label) }, singleLine = singleLine, minLines = if (singleLine) 1 else 2, modifier = Modifier.fillMaxWidth())
}

@Composable private fun SetupDecimalField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { candidate -> if (candidate.length <= 12 && candidate.all { it.isDigit() || it == '.' || it == '-' }) onValue(candidate) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
}
@Composable private fun SetupNumberField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { onValue(it.filter(Char::isDigit)) }, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
}

private fun validatePilotDraft(draft: HostedDoctorOnboardingDraft, latitudeText:String, longitudeText:String): String? = when {
    draft.displayName.trim().length < 2 -> "Enter the Doctor name."
    draft.registrationNumber.trim().length < 2 -> "Enter a valid registration number."
    draft.specialty.trim().length < 2 || draft.qualification.trim().length < 2 -> "Enter specialty and qualification."
    draft.clinicName.trim().length < 2 || draft.addressLine.trim().length < 3 -> "Enter the clinic name and full address."
    draft.city.trim().length < 2 || draft.state.trim().length < 2 -> "Enter city and state."
    !draft.pincode.matches(Regex("^[0-9]{6}$")) -> "Enter a 6-digit PIN code."
    !draft.clinicPhoneE164.matches(Regex("^\\+91[6-9][0-9]{9}$")) -> "Enter a valid 10-digit Indian clinic mobile number."
    (latitudeText.isBlank() != longitudeText.isBlank()) || (latitudeText.isNotBlank() && (draft.latitude == null || draft.longitude == null || draft.latitude !in -90.0..90.0 || draft.longitude !in -180.0..180.0)) -> "Enter both valid latitude and longitude values, or leave both blank."
    draft.weeklySessions.isEmpty() -> "Select at least one clinic day."
    draft.weeklySessions.any { !it.startsAt.matches(Regex("^[0-2][0-9]:[0-5][0-9]$")) || !it.endsAt.matches(Regex("^[0-2][0-9]:[0-5][0-9]$")) || it.startsAt >= it.endsAt } -> "Enter valid session start and end times."
    draft.weeklySessions.any { it.maxTokens !in 1..500 || it.averageConsultationMinutes !in 1..180 } -> "Max tokens or average consultation time is invalid."
    draft.futureBookingDays !in 0..90 || draft.rescheduleWindowDays !in 1..30 -> "Booking or reschedule days are outside the allowed range."
    else -> null
}