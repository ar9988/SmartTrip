package com.neungi.moyeo.views.home.viewmodel

import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent

sealed class HomeUiEvent {

    data object ShowFestivalDialog: HomeUiEvent()


}