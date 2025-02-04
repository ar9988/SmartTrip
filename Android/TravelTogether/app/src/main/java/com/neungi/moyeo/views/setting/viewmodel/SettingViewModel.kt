package com.neungi.moyeo.views.setting.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neungi.domain.usecase.GetUserInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    application: Application,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : AndroidViewModel(application), OnSettingClickListener {

    private val _settingUiState = MutableStateFlow<SettingUiState>(SettingUiState())
    val settingUiState = _settingUiState.asStateFlow()

    private val _settingUiEvent = MutableSharedFlow<SettingUiEvent>()
    val settingUiEvent = _settingUiEvent.asSharedFlow()

    /*** Datas ***/
    private val _userName = MutableStateFlow<String>("")
    val userName = _userName.asStateFlow()

    override fun onClickLogin() {
        viewModelScope.launch {
            _settingUiEvent.emit(SettingUiEvent.GoToLogin)
        }
    }

    private fun fetchUserName(): Flow<String> = flow {
        val name = getUserInfoUseCase.getUserName().first()
        emit(name)
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _userName.value = fetchUserName().first()
        }
    }
}