package com.dolo.doctor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            Surface(shape = RoundedCornerShape(15.dp), color = colors.surface, shadowElevation = 7.dp) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back from $title") }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) { DoctorBrand(); Text(title, color = colors.onSurfaceVariant, fontSize = 13.sp) }
    }
}

@Composable fun PrimaryAction(label: String, onClick: () -> Unit, enabled: Boolean = true, icon: ImageVector = Icons.Outlined.ArrowForward) {
    val colors = MaterialTheme.colorScheme
    val gradient = if (enabled) Brush.horizontalGradient(listOf(colors.primary, colors.secondary)) else Brush.horizontalGradient(listOf(colors.outline, colors.outline))
    val contentColor = if (enabled) colors.onPrimary else colors.onSurface
    Box(
        Modifier.fillMaxWidth().heightIn(min = 58.dp).shadow(9.dp, RoundedCornerShape(22.dp))
            .background(gradient, RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = contentColor)
            Spacer(Modifier.width(10.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color? = null) {
    val colors = MaterialTheme.colorScheme
    Card(modifier.semantics(mergeDescendants = true) { contentDescription = label + ": " + value }.shadow(8.dp, RoundedCornerShape(22.dp)), colors = CardDefaults.cardColors(containerColor = colors.surface), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = accent ?: colors.primary)
            Text(label, fontSize = 12.sp, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable fun ElevatedSection(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(22.dp)), colors = CardDefaults.cardColors(containerColor = colors.surface), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
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

enum class DoctorBottomDestination { HOME, QUEUE, APPOINTMENTS, PROFILE }

@Composable fun DoctorBottomBar(selected: DoctorBottomDestination, onHome: () -> Unit, onQueue: () -> Unit, onAppointments: () -> Unit, onProfile: () -> Unit, profileEnabled: Boolean = true) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), shadowElevation = 14.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomItem(Icons.Outlined.Home, "Home", selected == DoctorBottomDestination.HOME, onHome)
            BottomItem(Icons.Outlined.FormatListNumbered, "Queue", selected == DoctorBottomDestination.QUEUE, onQueue)
            BottomItem(Icons.Outlined.CalendarMonth, "Appointments", selected == DoctorBottomDestination.APPOINTMENTS, onAppointments)
            BottomItem(Icons.Outlined.Person, "Profile", selected == DoctorBottomDestination.PROFILE, onProfile, profileEnabled)
        }
    }
}

@Composable private fun BottomItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.sizeIn(minWidth = 72.dp, minHeight = 54.dp).clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { contentDescription = label + if (enabled) "" else ", unavailable" }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val color = if (!enabled) colors.outline else if (selected) colors.primary else colors.onSurfaceVariant
        Icon(icon, null, tint = color)
        Text(label, fontSize = 9.sp, color = color)
    }
}