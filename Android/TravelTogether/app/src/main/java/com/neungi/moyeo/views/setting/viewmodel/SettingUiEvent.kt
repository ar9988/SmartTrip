package com.neungi.moyeo.views.setting.viewmodel

sealed class SettingUiEvent {

    data object GoToLogin : SettingUiEvent()
}