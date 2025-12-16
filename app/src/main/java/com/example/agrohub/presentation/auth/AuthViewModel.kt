package com.example.agrohub.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.domain.model.User
import com.example.agrohub.domain.repository.AuthRepository
import com.example.agrohub.domain.util.Result
import com.example.agrohub.domain.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations.
 * Manages login and registration state for the UI layer.
 *
 * @property authRepository Repository for authentication operations
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()
    
    /**
     * Attempts to log in a user with email and password.
     * Updates loginState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> _loginState.value = UiState.Success(result.data.user)
                is Result.Error -> _loginState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Attempts to register a new user account.
     * Updates registerState through the following transitions:
     * Idle/Previous State → Loading → Success/Error
     *
     * @param email User's email address
     * @param username User's unique username
     * @param password User's password
     */
    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.register(email, username, password)) {
                is Result.Success -> _registerState.value = UiState.Success(result.data)
                is Result.Error -> _registerState.value = UiState.Error(result.exception.message)
                is Result.Loading -> { /* Should not happen */ }
            }
        }
    }
    
    /**
     * Logs out the current user by clearing stored authentication tokens.
     * This operation does not update any state as logout is typically
     * handled at the navigation level.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
