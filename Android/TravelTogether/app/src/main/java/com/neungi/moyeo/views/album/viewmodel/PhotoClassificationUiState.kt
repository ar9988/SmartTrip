package com.neungi.moyeo.views.album.viewmodel

import com.neungi.domain.model.Photo
import java.util.UUID

sealed class PhotoClassificationUiState(val id: String = UUID.randomUUID().toString()) {

    data class PhotoClassificationTitle(
        val title: String
    ) : PhotoClassificationUiState()

    data class PhotoClassificationPhoto(
        val photo: Photo
    ) : PhotoClassificationUiState()
}