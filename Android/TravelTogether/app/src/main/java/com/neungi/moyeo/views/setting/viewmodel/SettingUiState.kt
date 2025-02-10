package com.neungi.moyeo.views.setting.viewmodel

import com.neungi.moyeo.util.InputValidState

data class SettingUiState(
    val updateUserNameValidState: InputValidState = InputValidState.INIT,
    val updateUserProfileMessageValidState: InputValidState = InputValidState.INIT
) {
    val isUpdateBtnEnable: Boolean = (updateUserNameValidState == InputValidState.VALID)
}