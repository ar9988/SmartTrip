package com.neungi.moyeo.views.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neungi.domain.model.PhotoEntity
import com.neungi.domain.usecase.GetAuthUseCase
import com.neungi.moyeo.util.CommonUtils.validateEmail
import com.neungi.moyeo.util.CommonUtils.validatePassword
import com.neungi.moyeo.util.CommonUtils.validatePhoneNumber
import com.neungi.moyeo.util.InputValidState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val getAuthUseCase: GetAuthUseCase
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

    private val _joinEmail = MutableStateFlow<String>("")
    val joinEmail = _joinEmail

    private val _joinName = MutableStateFlow<String>("")
    val joinName = _joinName

    private val _joinPhoneNumber = MutableStateFlow<String>("")
    val joinPhoneNumber = _joinPhoneNumber

    private val _joinPassword = MutableStateFlow<String>("")
    val joinPassword = _joinPassword

    private val _joinPasswordAgain = MutableStateFlow<String>("")
    val joinPasswordAgain = _joinPasswordAgain

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

    override fun onClickJoinFinish() {
        viewModelScope.launch {
            getAuthUseCase.signUp(makeSignUpRequestBody())
            _authUiEvent.emit(AuthUiEvent.JoinSuccess)
        }
    }

    private fun makeSignUpRequestBody(): RequestBody {
        val metadata = mapOf(
            "email" to _joinEmail.value,
            "password" to _joinPassword.value,
            "name" to _joinName.value,
            "created_at" to "",
            "profile" to "",
            "updated_at" to ""
        )
        val json = Gson().toJson(metadata)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
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

    fun validJoinEmail(email: CharSequence) {
        when (email.isBlank()) {
            true -> _authUiState.update { it.copy(joinEmailValidState = InputValidState.BLANK) }

            else -> {
                when (validateEmail(email)) {
                    true -> _authUiState.update { it.copy(joinEmailValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(joinEmailValidState = InputValidState.NONE) }
                }
            }
        }
    }

    fun validJoinName(name: CharSequence) {
        when (name.isBlank()) {
            true -> _authUiState.update { it.copy(joinNameValidState = InputValidState.BLANK) }

            else -> _authUiState.update { it.copy(joinNameValidState = InputValidState.VALID) }
        }
    }

    fun validJoinPhoneNumber(phoneNumber: CharSequence) {
        when (phoneNumber.isBlank()) {
            true -> _authUiState.update { it.copy(joinPhoneNumberValidState = InputValidState.BLANK) }

            else -> {
                when (validatePhoneNumber(phoneNumber)) {
                    true -> _authUiState.update { it.copy(joinPhoneNumberValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(joinPhoneNumberValidState = InputValidState.NONE) }
                }
            }
        }
    }

    fun validJoinPassword(password: CharSequence) {
        when (password.isBlank()) {
            true -> _authUiState.update { it.copy(joinPasswordValidState = InputValidState.BLANK) }

            else -> {
                when (validatePassword(password)) {
                    true -> _authUiState.update { it.copy(joinPasswordValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(joinPasswordValidState = InputValidState.NONE) }
                }
            }
        }
    }

    fun validJoinPasswordAgain(passwordAgain: CharSequence) {
        when (passwordAgain.isBlank()) {
            true -> _authUiState.update { it.copy(joinPasswordAgainValidState = InputValidState.BLANK) }

            else -> {
                when (_joinPassword.value == passwordAgain.toString()) {
                    true -> _authUiState.update { it.copy(joinPasswordAgainValidState = InputValidState.VALID) }

                    else -> _authUiState.update { it.copy(joinPasswordAgainValidState = InputValidState.NONE) }
                }
            }
        }
    }
}