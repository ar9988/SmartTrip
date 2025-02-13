package com.neungi.moyeo.views.plan.tripviewmodel

import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent

sealed class TripUiEvent {

    data object TripEdit : TripUiEvent()

    data object TripAdd : TripUiEvent()

    data object TripDelete : TripUiEvent()

    data class TripInviteSuccess(val message: String, val tripId: Int) : TripUiEvent()

    data class TripInviteFail(val message: String) : TripUiEvent()
}