package com.neungi.moyeo.views.plan.tripviewmodel

import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent

sealed class TripUiEvent {
    data object TripEdit : TripUiEvent()
    data object TripAdd : TripUiEvent()
    data object TripDelete : TripUiEvent()
}