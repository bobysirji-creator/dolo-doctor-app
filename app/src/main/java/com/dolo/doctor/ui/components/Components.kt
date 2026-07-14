package com.dolo.doctor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dolo.doctor.ui.theme.*

private val doctorGradient = Brush.horizontalGradient(listOf(DoctorTeal, DoctorMint))

@Composable fun DoctorBrand(modifier: Modifier = Modifier) {
    Row(modifier.semantics(mergeDescendants = true) { contentDescription = "DO-LO Doctor" }, verticalAlignment = Alignment.CenterVertically) {
        Text("DO-", color = DoctorNavy, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text("LO", color = DoctorTeal, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(" DOCTOR", color = DoctorBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable fun PageHeader(title: String, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = 58.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Surface(shape = RoundedCornerShape(15.dp), color = Color.White, shadowElevation = 7.dp) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back from $title") }
            }
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) { DoctorBrand(); Text(title, color = DoctorMuted, fontSize = 13.sp) }
    }
}

@Composable fun PrimaryAction(label: String, onClick: () -> Unit, enabled: Boolean = true, icon: ImageVector = Icons.Outlined.ArrowForward) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 58.dp).shadow(9.dp, RoundedCornerShape(22.dp))
            .background(if (enabled) doctorGradient else Brush.horizontalGradient(listOf(Color.LightGray, Color.LightGray)), RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.White); Spacer(Modifier.width(10.dp)); Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
    }
}

@Composable fun MetricTile(label: String, value: String, modifier: Modifier = Modifier, accent: Color = DoctorTeal) {
    Card(modifier.shadow(8.dp, RoundedCornerShape(22.dp)), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Text(label, fontSize = 12.sp, color = DoctorMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable fun ElevatedSection(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(22.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FCFF)), elevation = CardDefaults.cardElevation(5.dp), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (subtitle != null) Text(subtitle, color = DoctorMuted, fontSize = 13.sp)
            content()
        }
    }
}

@Composable fun StatusPill(text: String, active: Boolean = true) {
    Surface(color = if (active) DoctorSurfaceAlt else Color(0xFFFFE9ED), shape = RoundedCornerShape(50)) {
        Text(text, Modifier.padding(horizontal = 11.dp, vertical = 6.dp), color = if (active) DoctorTeal else DoctorCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

enum class DoctorBottomDestination { HOME, QUEUE, APPOINTMENTS, PROFILE }

@Composable fun DoctorBottomBar(selected: DoctorBottomDestination, onHome: () -> Unit, onQueue: () -> Unit, onAppointments: () -> Unit, onProfile: () -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), shadowElevation = 14.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            BottomItem(Icons.Outlined.Home, "Home", selected == DoctorBottomDestination.HOME, onHome)
            BottomItem(Icons.Outlined.FormatListNumbered, "Queue", selected == DoctorBottomDestination.QUEUE, onQueue)
            BottomItem(Icons.Outlined.CalendarMonth, "Appointments", selected == DoctorBottomDestination.APPOINTMENTS, onAppointments)
            BottomItem(Icons.Outlined.Person, "Profile", selected == DoctorBottomDestination.PROFILE, onProfile)
        }
    }
}

@Composable private fun BottomItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.sizeIn(minWidth = 72.dp, minHeight = 54.dp).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, tint = if (selected) DoctorTeal else DoctorMuted)
        Text(label, fontSize = 9.sp, color = if (selected) DoctorTeal else DoctorMuted)
    }
}