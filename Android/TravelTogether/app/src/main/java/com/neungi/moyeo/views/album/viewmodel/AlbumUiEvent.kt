package com.neungi.moyeo.views.album.viewmodel

sealed class AlbumUiEvent {

    data object PhotoUpload : AlbumUiEvent()
}