package com.neungi.moyeo.views.auth.viewmodel

import com.neungi.moyeo.util.InputValidState

data class AuthUiState(
    val loginEmailValidState: InputValidState = InputValidState.INIT,
    val loginPasswordValidState: InputValidState = InputValidState.INIT
) {
    val isLoginBtnEnable: Boolean =
        ((loginEmailValidState == InputValidState.VALID) && (loginPasswordValidState == InputValidState.VALID))
}