package com.neungi.moyeo.views.auth.viewmodel

sealed class AuthUiEvent {

    data class LoginSuccess(val name: String) : AuthUiEvent()

    data object LoginFail : AuthUiEvent()

    data object GoToJoin : AuthUiEvent()

    data object GetProfileImage : AuthUiEvent()

    data object JoinSuccess : AuthUiEvent()

    data object JoinFail : AuthUiEvent()
}