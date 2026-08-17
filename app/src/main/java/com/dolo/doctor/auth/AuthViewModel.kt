package com.dolo.doctor.auth

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.doctor.data.model.UserRole
import com.dolo.doctor.hosted.HostedResult
import com.dolo.doctor.hosted.HttpHostedStaffApi
import java.util.concurrent.Executors

data class AuthUiState(
    val selectedRole: UserRole = UserRole.DOCTOR,
    val phone: String = "",
    val pin: String = "",
    val session: AuthSession? = null,
    val error: String? = null,
    val pinChangeMessage: String? = null,
    val controlledPilot: Boolean = false,
    val pilotActivation: Boolean = false,
    val pilotDoloId: String = "",
    val pilotInviteCode: String = "",
    val pilotCredential: String = "",
    val pilotLoading: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val hostedApi: HttpHostedStaffApi? = null,
    private val postToMain: ((() -> Unit) -> Unit) = { action ->
        Handler(Looper.getMainLooper()).post { action() }
    }
) : ViewModel() {
    private val executor = Executors.newSingleThreadExecutor()

    var uiState by mutableStateOf(AuthUiState(session = repository.restoredSession()))
        private set

    fun selectRole(role: UserRole) { uiState = uiState.copy(selectedRole = role, phone = "", pin = "", error = null) }
    fun selectLoginMode(controlledPilot: Boolean) { uiState = uiState.copy(controlledPilot = controlledPilot, error = null) }
    fun updatePhone(value: String) { uiState = uiState.copy(phone = CredentialValidator.normalizePhone(value), error = null) }
    fun updatePin(value: String) { uiState = uiState.copy(pin = value.filter(Char::isDigit).take(4), error = null) }
    fun selectPilotAction(activation: Boolean) { uiState = uiState.copy(pilotActivation = activation, error = null) }
    fun updatePilotDoloId(value: String) { uiState = uiState.copy(pilotDoloId = value.uppercase().filter { it.isLetterOrDigit() || it == '-' }.take(14), error = null) }
    fun updatePilotInviteCode(value: String) { uiState = uiState.copy(pilotInviteCode = value.trim().take(32), error = null) }
    fun updatePilotCredential(value: String) { uiState = uiState.copy(pilotCredential = value.take(128), error = null) }

    fun login() {
        when (val result = repository.login(uiState.selectedRole, uiState.phone, uiState.pin)) {
            is AuthResult.Success -> uiState = uiState.copy(session = result.session, pin = "", error = null)
            is AuthResult.Failure -> uiState = uiState.copy(error = result.message)
        }
    }

    fun loginPilot() {
        val doloId = uiState.pilotDoloId.trim().uppercase()
        val credential = uiState.pilotCredential
        if (!uiState.pilotActivation && !doloId.matches(Regex("^DLO-DOC-[0-9]{6}$"))) {
            uiState = uiState.copy(error = "Enter a valid Doctor DO-LO ID, for example DLO-DOC-000001.")
            return
        }
        if (uiState.pilotActivation && !uiState.pilotInviteCode.matches(Regex("^[A-Za-z0-9_-]{32}$"))) {
            uiState = uiState.copy(error = "Enter the complete 32-character invitation code.")
            return
        }
        if (credential.length < 8) {
            uiState = uiState.copy(error = "Enter the controlled pilot credential.")
            return
        }
        val api = hostedApi ?: run {
            uiState = uiState.copy(error = "Controlled pilot connection is unavailable.")
            return
        }
        uiState = uiState.copy(pilotLoading = true, error = null)
        executor.execute {
            val result = if (uiState.pilotActivation) api.activatePilot(uiState.pilotInviteCode, credential) else api.connectPilot(doloId, credential)
            postToMain {
                when (result) {
                    is HostedResult.Success -> when (val adopted = repository.adoptPilotDoctor(result.value.doloId, result.value.displayName)) {
                        is AuthResult.Success -> uiState = uiState.copy(session = adopted.session, pilotCredential = "", pilotInviteCode = "", pilotLoading = false, error = null)
                        is AuthResult.Failure -> {
                            api.logout()
                            uiState = uiState.copy(pilotLoading = false, error = adopted.message)
                        }
                    }
                    is HostedResult.Failure -> uiState = uiState.copy(pilotLoading = false, error = result.message)
                }
            }
        }
    }

    fun changePin(currentPin: String, newPin: String, confirmation: String): Boolean {
        val session = uiState.session ?: return false
        if (session.controlledPilot) {
            uiState = uiState.copy(pinChangeMessage = "Pilot credentials are managed by the hosted service.")
            return false
        }
        if (newPin != confirmation) {
            uiState = uiState.copy(pinChangeMessage = "New PINs do not match.")
            return false
        }
        return when (val result = repository.changePin(session, currentPin, newPin)) {
            is PinChangeResult.Success -> {
                uiState = uiState.copy(session = result.session, pinChangeMessage = "PIN changed successfully.")
                true
            }
            is PinChangeResult.Failure -> {
                uiState = uiState.copy(pinChangeMessage = result.message)
                false
            }
        }
    }

    fun clearPinChangeMessage() { uiState = uiState.copy(pinChangeMessage = null) }
    fun logout() { repository.logout(); uiState = AuthUiState() }

    override fun onCleared() {
        executor.shutdownNow()
        super.onCleared()
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val hostedApi: HttpHostedStaffApi
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository, hostedApi) as T
}