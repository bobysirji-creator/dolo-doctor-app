package com.dolo.doctor.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.dolo.doctor.ui.components.ElevatedSection
import com.dolo.doctor.ui.components.PageHeader
import com.dolo.doctor.ui.components.PrimaryAction

@Composable
fun ChangePinScreen(
    required: Boolean,
    isDoctor: Boolean,
    message: String?,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onClearMessage: () -> Unit,
    onSubmit: (String, String, String) -> Boolean
) {
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    BackHandler(enabled = required) {}
    LaunchedEffect(Unit) { onClearMessage() }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (required) {
            Text("Secure your account", style = MaterialTheme.typography.headlineMedium)
            Text("Replace the temporary PIN before opening clinic tools.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else PageHeader("Change login PIN", onBack)

        ElevatedSection(if (required) "Temporary PIN detected" else "Account security") {
            Icon(Icons.Outlined.Security, null, tint = MaterialTheme.colorScheme.primary)
            Text(if (required) "The PIN issued by the Doctor is valid only for initial access." else "Changing your PIN does not delete clinic data or end this session.")
            PinField("Current PIN", currentPin) { currentPin = it }
            PinField("New PIN", newPin) { newPin = it }
            PinField("Confirm new PIN", confirmation) { confirmation = it }
            Text("Use four digits. Common PINs such as 1234, 0000 and repeated digits are blocked.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (isDoctor) "There is no local Doctor PIN recovery. Record the new PIN securely." else "The Doctor can issue another temporary PIN if an Assistant forgets it.", color = MaterialTheme.colorScheme.error)
            PrimaryAction(
                "Save new PIN",
                {
                    if (onSubmit(currentPin, newPin, confirmation)) {
                        currentPin = ""; newPin = ""; confirmation = ""
                    }
                },
                enabled = currentPin.length == 4 && newPin.length == 4 && confirmation.length == 4,
                icon = Icons.Outlined.LockReset
            )
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        if (required) {
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Logout instead") }
        }
    }
}

@Composable
private fun PinField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValue(it.filter(Char::isDigit).take(4)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true
    )
}
