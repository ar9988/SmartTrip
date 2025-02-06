package com.neungi.moyeo.views.album.viewmodel

sealed class AlbumUiEvent {

    data object GoToAlbumDetail : AlbumUiEvent()

    data object BackToAlbum : AlbumUiEvent()

    data object SelectPlace : AlbumUiEvent()

    data object SelectPhoto : AlbumUiEvent()

    data object PhotoCommentSubmit : AlbumUiEvent()

    data object PhotoCommentUpdate : AlbumUiEvent()

    data object PhotoCommentDelete : AlbumUiEvent()

    data object PhotoCommentDeleteFinish : AlbumUiEvent()

    data object BackToAlbumDetail : AlbumUiEvent()

    data object PhotoDuplicated : AlbumUiEvent()

    data object PhotoUpload : AlbumUiEvent()

    data object GoToStorage : AlbumUiEvent()

    data object GoToClassifyPlaces : AlbumUiEvent()

    data object UpdatePhotoClassification : AlbumUiEvent()

    data object FinishPhotoClassificationUpdate : AlbumUiEvent()

    data object FinishPhotoUpload : AlbumUiEvent()
}