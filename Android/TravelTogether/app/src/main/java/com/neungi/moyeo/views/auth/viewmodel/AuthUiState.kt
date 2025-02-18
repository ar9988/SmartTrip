package com.neungi.moyeo.views.auth.viewmodel

import com.neungi.moyeo.util.InputValidState

data class AuthUiState(
    val loginEmailValidState: InputValidState = InputValidState.INIT,
    val loginPasswordValidState: InputValidState = InputValidState.INIT,
    val joinEmailValidState: InputValidState = InputValidState.INIT,
    val joinNameValidState: InputValidState = InputValidState.INIT,
    val joinPasswordValidState: InputValidState = InputValidState.INIT,
    val joinPasswordAgainValidState: InputValidState = InputValidState.INIT,
    val joinProfileMessageValidState: InputValidState = InputValidState.INIT
) {
    val isLoginBtnEnable: Boolean =
        ((loginEmailValidState == InputValidState.VALID) && (loginPasswordValidState == InputValidState.VALID))
    val isJoinBtnEnable: Boolean =
        ((joinEmailValidState == InputValidState.VALID) && (joinNameValidState == InputValidState.VALID) && (joinPasswordValidState == InputValidState.VALID) && (joinPasswordAgainValidState == InputValidState.VALID))
}