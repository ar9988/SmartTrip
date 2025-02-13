package com.neungi.moyeo.views.home.viewmodel

import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent

sealed class HomeUiEvent {

    data object ShowFestivalDialog: HomeUiEvent()

    data object ShowPlaceDialog: HomeUiEvent()

    data object GoToNotification:HomeUiEvent()


}