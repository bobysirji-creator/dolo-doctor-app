package com.dolo.doctor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.ui.theme.LocalDoloDoctorDarkTheme

@Composable fun DoctorBrand(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.semantics(mergeDescendants = true) { contentDescription = "DO-LO Doctor" }, verticalAlignment = Alignment.CenterVertically) {
        Text("DO-", color = colors.onSurface, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text("LO", color = colors.primary, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(" DOCTOR", color = colors.tertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable fun PageHeader(title: String, onBack: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().heightIn(min = 58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = colors.surface,
                shadowElevation = if (LocalDoloDoctorDarkTheme.current) 3.dp else 0.dp
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back from $title") }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) { DoctorBrand(); Text(title, color = colors.onSurfaceVariant, fontSize = 13.sp) }
    }
}

@Composable fun PrimaryAction(label: String, onClick: () -> Unit, enabled: Boolean = true, icon: ImageVector = Icons.Outlined.ArrowForward) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).semantics { contentDescription = label },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (LocalDoloDoctorDarkTheme.current) 3.dp else 0.dp,
            pressedElevation = if (LocalDoloDoctorDarkTheme.current) 1.dp else 0.dp
        )
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
@Composable fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color? = null) {
    val colors = MaterialTheme.colorScheme
    Card(modifier.semantics(mergeDescendants = true) { contentDescription = label + ": " + value }, colors = CardDefaults.cardColors(containerColor = colors.surface), elevation = CardDefaults.cardElevation(if (LocalDoloDoctorDarkTheme.current) 4.dp else 0.dp), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = accent ?: colors.primary)
            Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable fun ElevatedSection(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = colors.surface), elevation = CardDefaults.cardElevation(if (LocalDoloDoctorDarkTheme.current) 4.dp else 0.dp), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, Modifier.semantics { heading() }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (subtitle != null) Text(subtitle, color = colors.onSurfaceVariant, fontSize = 13.sp)
            content()
        }
    }
}

@Composable fun StatusPill(text: String, active: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = text
            stateDescription = text
        },
        color = if (active) colors.surfaceVariant else colors.errorContainer,
        shape = RoundedCornerShape(50)
    ) {
        Text(text, Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = if (active) colors.primary else colors.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

enum class DoctorBottomDestination { TODAY, APPOINTMENTS, CLINIC, MORE }

@Composable fun DoctorBottomBar(
    selected: DoctorBottomDestination,
    onToday: () -> Unit,
    onAppointments: () -> Unit,
    onClinic: () -> Unit,
    onMore: () -> Unit,
    clinicEnabled: Boolean = true
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = if (LocalDoloDoctorDarkTheme.current) 4.dp else 0.dp
    ) {
        BottomItem(Icons.Outlined.Today, "Today", selected == DoctorBottomDestination.TODAY, onToday)
        BottomItem(Icons.Outlined.CalendarMonth, "Appointments", selected == DoctorBottomDestination.APPOINTMENTS, onAppointments)
        BottomItem(Icons.Outlined.Business, "Clinic", selected == DoctorBottomDestination.CLINIC, onClinic, clinicEnabled)
        BottomItem(Icons.Outlined.MoreHoriz, "More", selected == DoctorBottomDestination.MORE, onMore)
    }
}

@Composable
private fun RowScope.BottomItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label, maxLines = 1) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        modifier = Modifier.semantics {
            contentDescription = label + if (enabled) "" else ", unavailable"
            stateDescription = if (selected) "Selected" else "Not selected"
        }
    )
}
@Composable fun DateRangeSelector(
    fromDate: java.time.LocalDate,
    toDate: java.time.LocalDate,
    onRangeChange: (java.time.LocalDate, java.time.LocalDate) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    fun showPicker(initial: java.time.LocalDate, onPicked: (java.time.LocalDate) -> Unit) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, day -> onPicked(java.time.LocalDate.of(year, month + 1, day)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Date range", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    showPicker(fromDate) { selected ->
                        onRangeChange(selected, if (selected.isAfter(toDate)) selected else toDate)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.DateRange, null)
                Spacer(Modifier.width(5.dp))
                Text(fromDate.toString(), fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    showPicker(toDate) { selected ->
                        onRangeChange(if (selected.isBefore(fromDate)) selected else fromDate, selected)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Outlined.Event, null)
                Spacer(Modifier.width(5.dp))
                Text(toDate.toString(), fontSize = 11.sp)
            }
        }
    }
}
