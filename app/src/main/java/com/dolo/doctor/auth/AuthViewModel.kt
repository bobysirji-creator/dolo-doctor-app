package com.dolo.doctor.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dolo.doctor.data.model.UserRole

data class AuthUiState(
    val selectedRole: UserRole = UserRole.DOCTOR,
    val phone: String = "",
    val pin: String = "",
    val session: AuthSession? = null,
    val error: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    var uiState by mutableStateOf(AuthUiState(session = repository.restoredSession()))
        private set

    fun selectRole(role: UserRole) { uiState = uiState.copy(selectedRole = role, phone = "", pin = "", error = null) }
    fun updatePhone(value: String) { uiState = uiState.copy(phone = CredentialValidator.normalizePhone(value), error = null) }
    fun updatePin(value: String) { uiState = uiState.copy(pin = value.filter(Char::isDigit).take(4), error = null) }
    fun login() {
        when (val result = repository.login(uiState.selectedRole, uiState.phone, uiState.pin)) {
            is AuthResult.Success -> uiState = uiState.copy(session = result.session, pin = "", error = null)
            is AuthResult.Failure -> uiState = uiState.copy(error = result.message)
        }
    }
    fun logout() { repository.logout(); uiState = AuthUiState() }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
}