package com.neungi.moyeo.views.album.viewmodel

import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.model.PhotoPlace

interface OnAlbumClickListener {

    fun onClickAlbum(photoAlbum: PhotoAlbum)

    fun onClickPhotoPlace(photoPlace: PhotoPlace)

    fun onClickPhoto(photo: Photo)

    fun onClickBackToAlbumDetail()

    fun onClickPhotoUpload()

    fun onClickGoToStorage()

    fun onClickFinishPhotoUpload()
}