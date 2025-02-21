package com.neungi.moyeo.views.home.viewmodel

import com.neungi.domain.model.Trip
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent

data class HomeScheduleCardUiState(
    val loginState: LoginState = LoginState.NOT_LOGGED_IN,
    val schedule: Trip? = null,
)

enum class LoginState {
    LOGGED_IN,
    NOT_LOGGED_IN
}

