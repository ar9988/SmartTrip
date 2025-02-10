package com.neungi.moyeo.views.setting.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.usecase.GetAuthUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.domain.usecase.SetUserInfoUseCase
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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val application: Application,
    private val getAuthUseCase: GetAuthUseCase,
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

    private val _updateUserProfileImage = MutableStateFlow<Uri?>(null)
    val updateUserProfileImage = _updateUserProfileImage.asStateFlow()

    private val _updateUserProfileFile = MutableStateFlow<MultipartBody.Part?>(null)
    val updateUserProfileFile = _updateUserProfileFile.asStateFlow()

    private val _updateUserName = MutableStateFlow<String>("")
    val updateUserName = _updateUserName

    private val _updateUserProfileMessage = MutableStateFlow<String>("")
    val updateUserProfileMessage = _updateUserProfileMessage

    override fun onClickLogin() {
        viewModelScope.launch {
            _settingUiEvent.emit(SettingUiEvent.GoToLogin)
        }
    }

    override fun onClickLogout() {
        viewModelScope.launch {
            setUserInfoUseCase.logOut()
            getUserInfo()
            _settingUiEvent.emit(SettingUiEvent.Logout)
        }
    }

    override fun onClickUpdateProfile() {
        viewModelScope.launch {
            _settingUiEvent.emit(SettingUiEvent.GoToUpdateProfile)
        }
    }

    override fun onClickUploadProfileImage() {
        viewModelScope.launch {
            _settingUiEvent.emit(SettingUiEvent.GoToUploadProfileImage)
        }
    }

    override fun onClickFinishUpdateProfile() {
        viewModelScope.launch {
            val response = getAuthUseCase.updateProfile(
                _updateUserProfileFile.value,
                prepareMetadataPart()
            )
            val data = response.data
            when ((response.status == ApiStatus.SUCCESS) && (data != null)) {
                true -> {
                    setUserInfoUseCase.setUserName(data.nickname)
                    setUserInfoUseCase.setUserProfile(data.profileImage)
                    setUserInfoUseCase.setUserProfileMessage(data.profile)
                    getUserInfo()
                    _settingUiEvent.emit(SettingUiEvent.UpdateProfileSuccess)
                }

                else -> {
                    _settingUiEvent.emit(SettingUiEvent.UpdateProfileFail)
                }
            }
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

    @SuppressLint("Recycle")
    private fun absolutelyPath(uri: Uri?): String? {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { application.contentResolver.query(uri, proj, null, null, null) }
        cursor?.moveToNext()
        val index = cursor?.getColumnIndex(MediaStore.MediaColumns.DATA)

        return index?.let { cursor.getString(index) }
    }

    private fun getMultipartBody() {
        val path = absolutelyPath(_updateUserProfileImage.value)
        path?.let {
            val file = File(path)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("profile_image", file.name, requestBody)
            _updateUserProfileFile.value = body
        }
    }

    private fun prepareMetadataPart(): RequestBody {
        val metadata = mapOf(
            "name" to _updateUserName.value,
            "profile" to _updateUserProfileMessage.value
        )
        val json = Gson().toJson(metadata)

        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _userId.value = fetchUserId().first()
            _userName.value = fetchUserName().first()
            _userEmail.value = fetchUserEmail().first()
            _userProfileMessage.value = fetchUserProfileMessage().first()
            _userProfileImage.value = fetchUserProfileImage().first()?.toUri()
            getMultipartBody()
        }
    }

    fun initUpdateUserInfo() {
        viewModelScope.launch {
            _updateUserProfileImage.value = _userProfileImage.value
            getMultipartBody()
            _updateUserName.value = _userName.value ?: ""
            _updateUserProfileMessage.value = _userProfileMessage.value ?: ""
        }
    }

    fun initProfile(uri: Uri?) {
        viewModelScope.launch {
            _updateUserProfileImage.value = uri
            getMultipartBody()
        }
    }

    fun validUpdateUserName(userName: CharSequence) {
        when (userName.isBlank()) {
            true -> _settingUiState.update { it.copy(updateUserNameValidState = InputValidState.BLANK) }

            false -> _settingUiState.update { it.copy(updateUserNameValidState = InputValidState.VALID) }
        }
    }
}