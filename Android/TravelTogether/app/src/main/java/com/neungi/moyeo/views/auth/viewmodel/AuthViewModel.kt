package com.neungi.moyeo.views.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.moyeo.util.CommonUtils.validateEmail
import com.neungi.moyeo.util.CommonUtils.validatePassword
import com.neungi.moyeo.util.InputValidState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application), OnAuthClickListener {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState())
    val authUiState = _authUiState.asStateFlow()

    private val _authUiEvent = MutableSharedFlow<AuthUiEvent>()
    val authUiEvent = _authUiEvent.asSharedFlow()

    /****** Datas ******/
    private val _loginEmail = MutableStateFlow<String>("")
    val loginEmail = _loginEmail

    private val _loginPassword = MutableStateFlow<String>("")
    val loginPassword = _loginPassword

    override fun onClickLogin() {
        viewModelScope.launch {
            // 추후 로그인 쪽 로직 추가 후 로그인 성공과 실패를 구분할 예정임.
            _authUiEvent.emit(AuthUiEvent.LoginSuccess)
        }
    }

    override fun onClickJoin() {
        viewModelScope.launch {
            _authUiEvent.emit(AuthUiEvent.GoToJoin)
        }
    }

    fun validateLoginEmail(email: CharSequence) {
        when (email.isBlank()) {
            true -> _authUiState.update { it.copy(loginEmailValidState = InputValidState.BLANK) }

            else -> {
                when (validateEmail(email)) {
                    true -> _authUiState.update { it.copy(loginEmailValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(loginEmailValidState = InputValidState.NONE) }
                }
            }
        }
    }

    fun validateLoginPassword(password: CharSequence) {
        when (password.isBlank()) {
            true -> _authUiState.update { it.copy(loginPasswordValidState = InputValidState.BLANK) }

            else -> {
                when (validatePassword(password)) {
                    true -> _authUiState.update { it.copy(loginPasswordValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(loginPasswordValidState = InputValidState.NONE) }
                }
            }
        }
    }
}