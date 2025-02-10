package com.neungi.moyeo.views.setting.viewmodel

sealed class SettingUiEvent {

    data object GoToLogin : SettingUiEvent()

    data object Logout : SettingUiEvent()

    data object GoToUpdateProfile : SettingUiEvent()

    data object GoToUploadProfileImage : SettingUiEvent()

    data object UpdateProfileSuccess : SettingUiEvent()

    data object UpdateProfileFail : SettingUiEvent()
}