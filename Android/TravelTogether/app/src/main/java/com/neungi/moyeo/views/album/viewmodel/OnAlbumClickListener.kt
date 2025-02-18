package com.neungi.moyeo.views.album.viewmodel

import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.model.PhotoPlace

interface OnAlbumClickListener {

    fun onClickAlbum(photoAlbum: PhotoAlbum)

    fun onClickBackToAlbum()

    fun onClickPhoto(photo: Photo)

    fun onClickBackToAlbumDetail()

    fun onClickPhotoUpload()

    fun onClickGoToStorage()

    fun onClickGoToClassifyPlaces()

    fun onClickUpdatePlaceName()

    fun onClickUpdatePhotoClassification(place: Int, index: Int)

    fun onClickFinishUpdatePhotoClassification()

    fun onClickFinishPhotoUpload()

    fun onClickDeletePhoto()

    fun onClickCommentSubmit()

    fun onClickCommentUpdate(comment: Comment)

    fun onClickCommentDelete(comment: Comment)

    fun onClickCommentDeleteFinish()
}