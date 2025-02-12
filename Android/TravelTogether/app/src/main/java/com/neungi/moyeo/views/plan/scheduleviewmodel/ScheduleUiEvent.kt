package com.neungi.moyeo.views.plan.scheduleviewmodel

sealed class ScheduleUiEvent {

    data object ScheduleAdd : ScheduleUiEvent()

    data object ScheduleEdit : ScheduleUiEvent()

    data object Schedule : ScheduleUiEvent()

    data object GoToScheduleInvite : ScheduleUiEvent()

    data class ScheduleInvite(
        val userName: String,
        val tripTitle: String,
        val token: String
    ) : ScheduleUiEvent()
}