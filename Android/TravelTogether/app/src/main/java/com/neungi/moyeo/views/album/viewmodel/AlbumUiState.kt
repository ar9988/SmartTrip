package com.neungi.moyeo.views.album.viewmodel

import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.util.InputValidState

data class AlbumUiState(
    val photoUploadValidState: EmptyState = EmptyState.EMPTY,
    val commentValidState: InputValidState = InputValidState.NONE
) {
    val isPhotoUploadBtnEnable: Boolean = (photoUploadValidState == EmptyState.NONE)
    val isCommentSubmitBtnEnable: Boolean = (commentValidState == InputValidState.VALID)
}
