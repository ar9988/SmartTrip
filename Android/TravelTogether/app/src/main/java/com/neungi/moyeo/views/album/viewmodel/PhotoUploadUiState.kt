package com.neungi.moyeo.views.album.viewmodel

import android.net.Uri
import java.util.UUID

sealed class PhotoUploadUiState(val id: String = UUID.randomUUID().toString()) {

    data class PhotoUploadButton(
        val title: String = "추가하기",
        val takenAt: Long = Long.MAX_VALUE
    ) : PhotoUploadUiState()

    data class UploadedPhoto(
        val photoUri: Uri,
        val takenAt: Long
    ) : PhotoUploadUiState()

    companion object {

        const val UPLOAD_VIEW_TYPE = 1
        const val PHOTO_VIEW_TYPE = 2
    }
}