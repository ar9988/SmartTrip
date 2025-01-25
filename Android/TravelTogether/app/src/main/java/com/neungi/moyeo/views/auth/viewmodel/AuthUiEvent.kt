package com.neungi.moyeo.views.auth.viewmodel

sealed class AuthUiEvent {

    data object LoginSuccess : AuthUiEvent()

    data object LoginFail : AuthUiEvent()

    data object GoToJoin : AuthUiEvent()
}