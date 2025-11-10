package me.idodanon.locationbasednotes.auth

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.core.content.edit

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(
        email: String,
        password: String
    ) {
        _authState.value = AuthState.Loading
        repo.login(email, password) { success, error ->
            _authState.value = if (success) AuthState.Authenticated(email)
            else AuthState.Error(error ?: "Error")
        }
    }

    fun signup(
        email: String,
        password: String
    ) {
        _authState.value = AuthState.Loading
        repo.signup(email, password) { success, error ->
            _authState.value = if (success) AuthState.Authenticated(email)
            else AuthState.Error(error ?: "Error")
        }
    }

    fun logout() {
        repo.logout()
        _authState.value = AuthState.Idle
    }

    fun saveLastUserEmail(context: Context, email: String) {
        val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("last_user_email", email) }
    }

    fun forgetLastEmail(context: Context) {
        val prefs = context.getSharedPreferences("notes_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("last_user_email", null) }
    }

    fun getLastEmail(context: Context): String? {
        val prefs = context.getSharedPreferences("notes_prefs", MODE_PRIVATE)
        return prefs.getString("last_user_email", null)
    }


}
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val email: String) : AuthState()
    data class Error(val message: String) : AuthState()
}