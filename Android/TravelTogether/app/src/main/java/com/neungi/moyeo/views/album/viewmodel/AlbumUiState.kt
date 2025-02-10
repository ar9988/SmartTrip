package com.neungi.moyeo.views.album.viewmodel

import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.util.EnterState
import com.neungi.moyeo.util.InputValidState

data class AlbumUiState(
    val photoUploadValidState: EmptyState = EmptyState.EMPTY,
    val commentValidState: InputValidState = InputValidState.NONE,
    val enterDirectlyState: EnterState = EnterState.NONE,
    val updatePlaceNameState: InputValidState = InputValidState.NONE,
    val newPlaceNameState: InputValidState = InputValidState.NONE
) {
    val isPhotoUploadBtnEnable: Boolean = (photoUploadValidState == EmptyState.NONE)
    val isCommentSubmitBtnEnable: Boolean = (commentValidState == InputValidState.VALID)
    val isPlaceNameEtEnable: Boolean = (enterDirectlyState == EnterState.VALID)
    val isUpdatePlaceNameBtnEnable: Boolean = (updatePlaceNameState == InputValidState.VALID)
    val isPhotoPlaceUpdateBtnEnable: Boolean = (newPlaceNameState == InputValidState.VALID)
}