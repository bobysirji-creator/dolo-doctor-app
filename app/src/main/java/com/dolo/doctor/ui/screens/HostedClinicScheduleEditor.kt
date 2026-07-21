package com.dolo.doctor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dolo.doctor.hosted.HostedClinicSchedule
import com.dolo.doctor.hosted.HostedClinicScheduleUiState
import com.dolo.doctor.hosted.HostedScheduleException
import com.dolo.doctor.hosted.HostedWeeklySession
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PrimaryAction
import com.dolo.doctor.ui.components.StatusPill
import java.time.LocalDate

private val scheduleDayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun HostedClinicScheduleEditor(
    state: HostedClinicScheduleUiState,
    queueLoading: Boolean,
    onRefresh: () -> Unit,
    onSaveSchedule: (HostedClinicSchedule) -> Unit,
    onSaveException: (String, HostedScheduleException) -> Unit
) {
    val schedule = state.schedule
    if (schedule == null) {
        ElevatedSection("Hosted clinic schedule", state.message) {
            StatusPill(if (state.error) "Needs attention" else "Not loaded", false)
            PrimaryAction("Load hosted schedule", onRefresh, enabled = !state.loading && !queueLoading)
        }
        return
    }

    var selectedDay by remember(schedule.clinicId) { mutableIntStateOf(LocalDate.now().dayOfWeek.value % 7) }
    var futureDays by remember(schedule) { mutableStateOf(schedule.futureBookingDays.toString()) }
    var rescheduleDays by remember(schedule) { mutableStateOf(schedule.rescheduleWindowDays.toString()) }
    var rows by remember(schedule) { mutableStateOf(completeScheduleRows(schedule.weeklySessions)) }
    var exceptionDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var exceptionSession by remember { mutableStateOf<String?>(null) }
    var exceptionEnabled by remember { mutableStateOf(false) }
    var exceptionReason by remember { mutableStateOf("") }

    val future = futureDays.toIntOrNull()
    val reschedule = rescheduleDays.toIntOrNull()
    val scheduleValid = future != null && future in 0..90 && reschedule != null && reschedule in 1..30 && rows.all(::validScheduleRow)
    val parsedExceptionDate = runCatching { LocalDate.parse(exceptionDate) }.getOrNull()
    val exceptionValid = parsedExceptionDate != null && !parsedExceptionDate.isBefore(LocalDate.now()) && !parsedExceptionDate.isAfter(LocalDate.now().plusDays(365)) && exceptionReason.trim().length <= 200

    ElevatedSection("Hosted clinic schedule", "Authoritative Patient booking availability") {
        Text("Changes affect hosted Patient dates only. Existing local clinic data is not uploaded or replaced.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                futureDays,
                { futureDays = it.filter(Char::isDigit).take(2) },
                Modifier.weight(1f),
                label = { Text("Future days") },
                supportingText = { Text("0 = today only") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                rescheduleDays,
                { rescheduleDays = it.filter(Char::isDigit).take(2) },
                Modifier.weight(1f),
                label = { Text("Reschedule days") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
        Text("Recurring weekly sessions", fontWeight = FontWeight.Bold)
        scheduleDayNames.chunked(4).forEach { dayRow ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                dayRow.forEach { name ->
                    val day = scheduleDayNames.indexOf(name)
                    val open = rows.any { it.dayOfWeek == day && it.bookingEnabled }
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        label = { Text(if (open) name else "$name off") },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - dayRow.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        listOf("MORNING", "EVENING").forEach { sessionName ->
            val row = rows.first { it.dayOfWeek == selectedDay && it.session == sessionName }
            HostedSessionRuleEditor(row) { updated ->
                rows = rows.map { current ->
                    if (current.dayOfWeek == updated.dayOfWeek && current.session == updated.session) updated else current
                }
            }
        }
        Text(state.message, color = if (state.error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        PrimaryAction(
            "Save hosted weekly schedule",
            { onSaveSchedule(schedule.copy(futureBookingDays = future ?: 0, rescheduleWindowDays = reschedule ?: 10, weeklySessions = rows)) },
            enabled = scheduleValid && !state.loading && !queueLoading
        )
        OutlinedButton(onRefresh, Modifier.fillMaxWidth(), enabled = !state.loading && !queueLoading) { Text("Refresh hosted schedule") }
    }

    ElevatedSection("Date-specific booking control", "Close or reopen a whole date or one session") {
        OutlinedTextField(exceptionDate, { exceptionDate = it.take(10) }, Modifier.fillMaxWidth(), label = { Text("Date YYYY-MM-DD") }, singleLine = true)
        Text("Scope", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(null to "Whole day", "MORNING" to "Morning", "EVENING" to "Evening").forEach { (value, label) ->
                FilterChip(selected = exceptionSession == value, onClick = { exceptionSession = value }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(if (exceptionEnabled) "Reopen booking" else "Close booking", fontWeight = FontWeight.Bold)
                Text("Existing appointments are retained. Reopen applies only to a normally enabled weekly session.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(exceptionEnabled, { exceptionEnabled = it })
        }
        OutlinedTextField(exceptionReason, { exceptionReason = it.take(200) }, Modifier.fillMaxWidth(), label = { Text("Reason") }, supportingText = { Text("${exceptionReason.length}/200") }, minLines = 2)
        PrimaryAction(
            if (exceptionEnabled) "Reopen selected date/session" else "Close selected date/session",
            { onSaveException(schedule.clinicId, HostedScheduleException(exceptionDate, exceptionSession, exceptionEnabled, exceptionReason.trim())) },
            enabled = exceptionValid && !state.loading && !queueLoading
        )
        if (schedule.exceptions.isEmpty()) {
            Text("No date-specific controls in the next 90 days.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            schedule.exceptions.sortedBy { it.serviceDate }.forEach { exception ->
                HorizontalDivider()
                Text("${exception.serviceDate} | ${exception.session ?: "Whole day"} | ${if (exception.bookingEnabled) "Open" else "Closed"}", fontWeight = FontWeight.Bold)
                Text(exception.reason.ifBlank { "No reason supplied" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HostedSessionRuleEditor(row: HostedWeeklySession, onChange: (HostedWeeklySession) -> Unit) {
    ElevatedSection(row.session.lowercase().replaceFirstChar(Char::uppercase), if (row.bookingEnabled) "Accepting hosted bookings" else "Recurring session off") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Booking enabled", fontWeight = FontWeight.Bold)
            Switch(row.bookingEnabled, { onChange(row.copy(bookingEnabled = it)) })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(row.startsAt, { onChange(row.copy(startsAt = it.take(5))) }, Modifier.weight(1f), label = { Text("Starts HH:mm") }, singleLine = true)
            OutlinedTextField(row.endsAt, { onChange(row.copy(endsAt = it.take(5))) }, Modifier.weight(1f), label = { Text("Ends HH:mm") }, singleLine = true)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(row.maxTokens.toString(), { value -> onChange(row.copy(maxTokens = value.filter(Char::isDigit).take(3).toIntOrNull() ?: 0)) }, Modifier.weight(1f), label = { Text("Max tokens") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            OutlinedTextField(row.averageConsultationMinutes.toString(), { value -> onChange(row.copy(averageConsultationMinutes = value.filter(Char::isDigit).take(3).toIntOrNull() ?: 0)) }, Modifier.weight(1f), label = { Text("Avg minutes") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        }
    }
}

private fun completeScheduleRows(existing: List<HostedWeeklySession>): List<HostedWeeklySession> = buildList {
    for (day in 0..6) {
        for (session in listOf("MORNING", "EVENING")) {
            add(
                existing.firstOrNull { it.dayOfWeek == day && it.session == session }
                    ?: HostedWeeklySession(day, session, if (session == "MORNING") "09:00" else "17:00", if (session == "MORNING") "12:00" else "20:00", 30, 12, false)
            )
        }
    }
}

private fun validScheduleRow(row: HostedWeeklySession): Boolean =
    row.startsAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) &&
        row.endsAt.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d")) &&
        row.startsAt < row.endsAt &&
        row.maxTokens in 1..500 &&
        row.averageConsultationMinutes in 1..180
