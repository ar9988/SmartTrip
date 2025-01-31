package com.neungi.moyeo.views.aiplanning.viewmodel

sealed class AiPlanningUiEvent {

    data object GoToSelectLocal : AiPlanningUiEvent()

    data object GoToDestination : AiPlanningUiEvent()

    data object GoToSearchPlace : AiPlanningUiEvent()

    data object PopBackToDestiination :AiPlanningUiEvent()


}