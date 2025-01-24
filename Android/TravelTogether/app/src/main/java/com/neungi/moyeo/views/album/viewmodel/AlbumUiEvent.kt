package com.neungi.moyeo.views.album.viewmodel

sealed class AlbumUiEvent {

    data object GoToAlbumDetail : AlbumUiEvent()

    data object SelectPlace : AlbumUiEvent()

    data object PhotoUpload : AlbumUiEvent()

    data object GoToStorage : AlbumUiEvent()

    data object FinishPhotoUpload : AlbumUiEvent()
}