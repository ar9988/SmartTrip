package com.neungi.moyeo.views.album.viewmodel

sealed class AlbumUiEvent {

    data object GetAlbumsSuccess : AlbumUiEvent()

    data object GetAlbumsFail : AlbumUiEvent()

    data object GoToAlbumDetail : AlbumUiEvent()

    data object GetAlbumDetailFail : AlbumUiEvent()

    data object BackToAlbum : AlbumUiEvent()

    data object SelectPhoto : AlbumUiEvent()

    data object BackToAlbumDetail : AlbumUiEvent()

    data object PhotoDuplicated : AlbumUiEvent()

    data object PhotoUpload : AlbumUiEvent()

    data object GoToStorage : AlbumUiEvent()

    data object GoToClassifyPlaces : AlbumUiEvent()

    data object UpdatePhotoClassification : AlbumUiEvent()

    data object FinishPhotoClassificationUpdate : AlbumUiEvent()

    data object UpdatePhotoPlaceName : AlbumUiEvent()

    data object UpdatePhotoPlaceNameSuccess : AlbumUiEvent()

    data object PhotoUploadFail : AlbumUiEvent()

    data object FinishPhotoUpload : AlbumUiEvent()

    data object DeletePhotoSuccess : AlbumUiEvent()

    data object DeletePhotoFail : AlbumUiEvent()

    data object PhotoDownload : AlbumUiEvent()

    data object GetPhotoCommentsFail : AlbumUiEvent()

    data object PhotoCommentSubmitSuccess : AlbumUiEvent()

    data object PhotoCommentSubmitFail : AlbumUiEvent()

    data object PhotoCommentUpdateSuccess : AlbumUiEvent()

    data object PhotoCommentUpdateFail : AlbumUiEvent()

    data object PhotoCommentDelete : AlbumUiEvent()

    data object PhotoCommentDeleteFinish : AlbumUiEvent()

    data object PhotoCommentDeleteFail : AlbumUiEvent()
}