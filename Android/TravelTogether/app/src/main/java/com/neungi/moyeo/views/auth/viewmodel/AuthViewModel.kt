package com.neungi.moyeo.views.auth.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.User
import com.neungi.domain.usecase.GetAuthUseCase
import com.neungi.domain.usecase.SetFCMUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.domain.usecase.SetUserInfoUseCase
import com.neungi.moyeo.util.CommonUtils.validateEmail
import com.neungi.moyeo.util.CommonUtils.validatePassword
import com.neungi.moyeo.util.CommonUtils.validatePhoneNumber
import com.neungi.moyeo.util.InputValidState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val getAuthUseCase: GetAuthUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val setUserInfoUseCase: SetUserInfoUseCase,
    private val setFCMUseCase: SetFCMUseCase
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

    private val _joinProfileMessage = MutableStateFlow<String>("")
    val joinProfileMessage = _joinProfileMessage

    private val _joinProfileImageUri = MutableStateFlow<Uri?>(null)
    val joinProfileImageUri = _joinProfileImageUri.asStateFlow()

    private val _joinProfileImageFile = MutableStateFlow<MultipartBody.Part?>(null)
    val joinProfileImageFile = _joinProfileImageFile.asStateFlow()

    override fun onClickLogin() {
        viewModelScope.launch {
            val response = getAuthUseCase.login(makeLoginRequestBody())
            val status = response.status
            val data = response.data
            when (status == ApiStatus.SUCCESS && data != null) {
                true -> {
                    saveUserInfo(data)
                }

                else -> {
                    _authUiEvent.emit(AuthUiEvent.LoginFail)
                }
            }
        }
    }

    override fun onClickJoin() {
        viewModelScope.launch {
            _authUiEvent.emit(AuthUiEvent.GoToJoin)
        }
    }

    override fun onClickProfile() {
        viewModelScope.launch {
            _authUiEvent.emit(AuthUiEvent.GetProfileImage)
        }
    }

    override fun onClickJoinFinish() {
        viewModelScope.launch {
            val response = getAuthUseCase.signUp(makeSignUpRequestBody())
            when (response.status) {
                ApiStatus.SUCCESS -> {
                    initJoinInfo()
                    _authUiEvent.emit(AuthUiEvent.JoinSuccess)
                }

                else -> {
                    _authUiEvent.emit(AuthUiEvent.JoinFail)
                }
            }
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

    private fun makeLoginRequestBody(): RequestBody {
        val metadata = mapOf(
            "email" to _loginEmail.value,
            "password" to _loginPassword.value
        )
        val json = Gson().toJson(metadata)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun saveUserInfo(userInfo: Pair<User, String>) {
        viewModelScope.launch {
            setUserInfoUseCase.setJWT(userInfo.second.split(" ").last())
            setUserInfoUseCase.setUserId(userInfo.first.id)
            setUserInfoUseCase.setUserEmail(userInfo.first.email)
            setUserInfoUseCase.setUserName(userInfo.first.nickname)
            Timber.d("Name: ${userInfo.first.nickname}")
            setUserInfoUseCase.setUserProfile(userInfo.first.profile)
            initLoginInfo()
            _authUiEvent.emit(AuthUiEvent.LoginSuccess(getUserName().first() ?: ""))
        }
    }

    private fun initLoginInfo() {
        _loginEmail.value = ""
        _loginPassword.value = ""
    }

    private fun initJoinInfo() {
        _joinEmail.value = ""
        _joinPassword.value = ""
        _joinPasswordAgain.value = ""
        _joinName.value = ""
        _joinPhoneNumber.value = ""
    }

    private fun getUserName(): Flow<String?> = flow {
        val name = getUserInfoUseCase.getUserName().first()
        emit(name)
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

    fun validJoinProfileMessage(profileMessage: CharSequence) {
        when (profileMessage.isBlank()) {
            true -> {
                _authUiState.update { it.copy(joinProfileMessageValidState = InputValidState.NONE) }
            }

            else -> {
                _authUiState.update { it.copy(joinProfileMessageValidState = InputValidState.VALID) }
            }
        }
    }

    fun initProfile(uri: Uri?, file: MultipartBody.Part?) {
        _joinProfileImageUri.value = uri
        _joinProfileImageFile.value = file
    }
}