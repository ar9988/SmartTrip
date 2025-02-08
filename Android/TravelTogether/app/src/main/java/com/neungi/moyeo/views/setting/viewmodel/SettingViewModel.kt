package com.neungi.moyeo.views.setting.viewmodel

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.domain.usecase.SetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    application: Application,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val setUserInfoUseCase: SetUserInfoUseCase
) : AndroidViewModel(application), OnSettingClickListener {

    private val _settingUiState = MutableStateFlow<SettingUiState>(SettingUiState())
    val settingUiState = _settingUiState.asStateFlow()

    private val _settingUiEvent = MutableSharedFlow<SettingUiEvent>()
    val settingUiEvent = _settingUiEvent.asSharedFlow()

    /*** Datas ***/
    private val _userId = MutableStateFlow<String?>(null)
    val userId = _userId.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail = _userEmail.asStateFlow()

    private val _userProfileMessage = MutableStateFlow<String?>(null)
    val userProfileMessage = _userProfileMessage.asStateFlow()

    private val _userProfileImage = MutableStateFlow<Uri?>(null)
    val userProfileImage = _userProfileImage.asStateFlow()

    override fun onClickLogin() {
        viewModelScope.launch {
            _settingUiEvent.emit(SettingUiEvent.GoToLogin)
        }
    }

    override fun onClickLogout() {
        viewModelScope.launch {
            setUserInfoUseCase.logOut()
            getUserInfo()
        }
    }

    private fun fetchUserId(): Flow<String?> = flow {
        val id = getUserInfoUseCase.getUserId().first()
        emit(id)
    }

    private fun fetchUserName(): Flow<String?> = flow {
        val name = getUserInfoUseCase.getUserName().first()
        emit(name)
    }

    private fun fetchUserEmail(): Flow<String?> = flow {
        val email = getUserInfoUseCase.getUserEmail().first()
        emit(email)
    }

    private fun fetchUserProfileMessage(): Flow<String?> = flow {
        val message = getUserInfoUseCase.getUserProfileMessage().first()
        emit(message)
    }

    private fun fetchUserProfileImage(): Flow<String?> = flow {
        val profileImage = getUserInfoUseCase.getUserProfile().first()
        emit(profileImage)
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _userId.value = fetchUserId().first()
            _userName.value = fetchUserName().first()
            _userEmail.value = fetchUserEmail().first()
            _userProfileMessage.value = fetchUserProfileMessage().first()
            _userProfileImage.value = fetchUserProfileImage().first()?.toUri()
            Timber.d("Id: ${_userId.value}")
        }
    }
}