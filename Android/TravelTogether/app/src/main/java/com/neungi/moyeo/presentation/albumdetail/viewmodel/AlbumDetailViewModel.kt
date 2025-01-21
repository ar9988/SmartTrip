package com.neungi.moyeo.presentation.albumdetail.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class AlbumDetailViewModel : ViewModel(), AlbumDetailUiAction {

    private val _uiState: MutableAlbumDetailUiState = MutableAlbumDetailUiState()
    val uiState: AlbumDetailUiState = _uiState

    private val _channel = Channel<AlbumDetailUiEvent>()
    val channel: Flow<AlbumDetailUiEvent> = _channel.receiveAsFlow()

    init {
        getAlbumPhotos()
    }

    private fun getAlbumPhotos() {

    }
}