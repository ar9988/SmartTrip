package com.neungi.moyeo.views.aiplanning.viewmodel

sealed class AiPlanningUiEvent {

    data object GoToSelectLocal : AiPlanningUiEvent()

    data object GoToDestination : AiPlanningUiEvent()

    data object GoToSearchPlace : AiPlanningUiEvent()

    data object PopBackToDestination :AiPlanningUiEvent()

    data object GoToTheme :AiPlanningUiEvent()

    data object LimitToast:AiPlanningUiEvent()

    data object ShowFestivalDialog:AiPlanningUiEvent()




}