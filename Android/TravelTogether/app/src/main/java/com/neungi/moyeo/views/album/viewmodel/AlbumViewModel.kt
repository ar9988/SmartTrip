package com.neungi.moyeo.views.album.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getDoubleOrNull
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.neungi.domain.model.ApiResult
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.model.PhotoEntity
import com.neungi.domain.model.PhotoPlace
import com.neungi.domain.usecase.GetAlbumsUseCase
import com.neungi.domain.usecase.GetCommentsUseCase
import com.neungi.domain.usecase.GetPhotoLocationsUseCase
import com.neungi.domain.usecase.GetPhotosUseCase
import com.neungi.domain.usecase.GetUserInfoUseCase
import com.neungi.moyeo.util.CommonUtils.calculateLocation
import com.neungi.moyeo.util.CommonUtils.convertToDegree
import com.neungi.moyeo.util.CommonUtils.formatLongToDateTime
import com.neungi.moyeo.util.CommonUtils.isPhotoTakenInKorea
import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.util.EnterState
import com.neungi.moyeo.util.InputValidState
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val application: Application,
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getPhotoLocationsUseCase: GetPhotoLocationsUseCase,
    private val getPhotosUseCase: GetPhotosUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : AndroidViewModel(application), OnAlbumClickListener {

    /****** UiState, UiEvent ******/
    private val _albumUiState = MutableStateFlow<AlbumUiState>(AlbumUiState())
    val albumUiState = _albumUiState.asStateFlow()

    private val _albumUiEvent = MutableSharedFlow<AlbumUiEvent>()
    val albumUiEvent = _albumUiEvent.asSharedFlow()

    private val _albumsState =
        MutableStateFlow<ApiResult<List<PhotoAlbum>>>(ApiResult.success(emptyList()))
    val albumsState = _albumsState.asStateFlow()

    private val _photoUploadState =
        MutableStateFlow<ApiResult<List<Photo>>>(ApiResult.success(emptyList()))
    val photoUploadState = _photoUploadState.asStateFlow()

    private val _commentSubmitState = MutableStateFlow<ApiResult<Boolean>>(ApiResult.success(false))
    val commentSubmitState = _commentSubmitState.asStateFlow()

    /****** Datas ******/
    private val _userId = MutableStateFlow<String?>(null)
    val userId = _userId.asStateFlow()

    private val _photoAlbums = MutableStateFlow<List<PhotoAlbum>>(emptyList())
    val photoAlbums = _photoAlbums.asStateFlow()

    private val _markers = MutableStateFlow<List<Pair<String, List<MarkerData>>>>(emptyList())
    val markers = _markers.asStateFlow()

    private val _tempPhotos = MutableStateFlow<List<MarkerData>>(emptyList())
    val tempPhotos = _tempPhotos.asStateFlow()

    private val _newMarkers = MutableStateFlow<List<Pair<String, List<MarkerData>>>>(emptyList())
    val newMarkers = _newMarkers.asStateFlow()

    private val _updatePlaceIndex = MutableStateFlow<Int>(-1)
    val updatePlaceIndex = _updatePlaceIndex.asStateFlow()

    private val _updatePlaceName = MutableStateFlow<String>("")
    val updatePlaceName = _updatePlaceName

    private val _tempNewPlaceName = MutableStateFlow<String>("")
    val tempNewPlaceName = _tempNewPlaceName

    private val _tempClassifiedPhotoIndex = MutableStateFlow<Pair<Int, Int>>(Pair(-1, -1))
    val tempClassifiedPhotoIndex = _tempClassifiedPhotoIndex.asStateFlow()

    private val _photoClassifications =
        MutableStateFlow<List<PhotoClassificationUiState>>(emptyList())
    val photoClassifications = _photoClassifications.asStateFlow()

    private val _selectedPhotoAlbum = MutableStateFlow<PhotoAlbum?>(null)
    val selectedPhotoAlbum = _selectedPhotoAlbum.asStateFlow()

    private val _photoPlaces = MutableStateFlow<List<PhotoPlace>>(emptyList())
    val photoPlaces = _photoPlaces.asStateFlow()

    private val _selectedPhotoPlace = MutableStateFlow<PhotoPlace?>(null)
    val selectedPhotoPlace = _selectedPhotoPlace.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos = _photos.asStateFlow()

    private val _selectedPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val selectedPhotos = _selectedPhotos.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<Photo?>(null)
    val selectedPhoto = _selectedPhoto.asStateFlow()

    private val _isSelectedPhotoRemove = MutableStateFlow<Boolean>(false)
    val isSelectedPhotoRemove = _isSelectedPhotoRemove.asStateFlow()

    private val _selectedPhotoComments = MutableStateFlow<List<Comment>>(emptyList())
    val selectedPhotoComments = _selectedPhotoComments.asStateFlow()

    private val _selectedPhotoComment = MutableStateFlow<Comment?>(null)
    val selectedPhotoComment = _selectedPhotoComment.asStateFlow()

    private val _commentInput = MutableStateFlow<String>("")
    val commentInput = _commentInput

    private val _commentUpdateInput = MutableStateFlow<String>("")
    val commentUpdateInput = _commentUpdateInput

    private val _uploadPhotos = MutableStateFlow<List<PhotoUploadUiState>>(emptyList())
    val uploadPhotos = _uploadPhotos.asStateFlow()

    private val _uploadMultiparts = MutableStateFlow<List<MultipartBody.Part>>(emptyList())
    val uploadMultiparts = _uploadMultiparts.asStateFlow()

    init {
        viewModelScope.launch {
            _userId.value = fetchUserId().first()
            initAlbums()
        }
    }

    override fun onClickAlbum(photoAlbum: PhotoAlbum) {
        viewModelScope.launch {
            _selectedPhotoAlbum.value = photoAlbum
            initPhotos()
        }
    }

    override fun onClickBackToAlbum() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.BackToAlbum)
        }
    }

    override fun onClickPhoto(photo: Photo) {
        viewModelScope.launch {
            _selectedPhoto.value = photo
            _isSelectedPhotoRemove.value = (_userId.value == (_selectedPhoto.value?.userId ?: "-1"))
            initComments()
            _albumUiEvent.emit(AlbumUiEvent.SelectPhoto)
        }
    }

    override fun onClickBackToAlbumDetail() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.BackToAlbumDetail)
        }
    }

    override fun onClickPhotoUpload() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.PhotoUpload)
        }
    }

    override fun onClickGoToStorage() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.GoToStorage)
        }
    }

    override fun onClickGoToClassifyPlaces() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.GoToClassifyPlaces)
        }
    }

    override fun onClickUpdatePlaceName() {
        viewModelScope.launch {
            val index = _updatePlaceIndex.value
            val newTempPlaces = _newMarkers.value.toMutableList()
            newTempPlaces[index] = Pair(_updatePlaceName.value, newTempPlaces[index].second)
            _newMarkers.value = newTempPlaces.toList()
            _albumUiEvent.emit(AlbumUiEvent.UpdatePhotoPlaceNameSuccess)
        }
    }

    override fun onClickUpdatePhotoClassification(place: Int, index: Int) {
        viewModelScope.launch {
            _tempClassifiedPhotoIndex.value = Pair(place, index)
            _albumUiEvent.emit(AlbumUiEvent.UpdatePhotoClassification)
        }
    }

    override fun onClickFinishUpdatePhotoClassification() {
        viewModelScope.launch {
            updatePhotoPlace()
            _albumUiEvent.emit(AlbumUiEvent.FinishPhotoClassificationUpdate)
        }
    }

    override fun onClickFinishPhotoUpload() {
        viewModelScope.launch {
            val albumId = _selectedPhotoAlbum.value?.id ?: "-1"
            val photos = mutableListOf<PhotoEntity>()
            _newMarkers.value.forEach { place ->
                place.second.forEach { photo ->
                    photos.add(
                        PhotoEntity(
                            place = place.first,
                            body = photo.body,
                            latitude = photo.photo.latitude,
                            longitude = photo.photo.longitude,
                            takenAt = photo.photo.takenAt
                        )
                    )
                }
            }

            val averageLocation = calculateLocation(photos)
            photos.forEachIndexed { index, photoEntity ->
                if ((photoEntity.latitude == 0.0) && (photoEntity.longitude == 0.0)) {
                    photos[index] = PhotoEntity(
                        photoEntity.place,
                        photoEntity.body,
                        averageLocation.latitude,
                        averageLocation.longitude,
                        photoEntity.takenAt
                    )
                }
                Timber.d("Lat: ${photoEntity.latitude}, Lng: ${photoEntity.longitude}")
            }

            val chunked = photos.chunked(10)
            var flag = true
            for (chunk in chunked) {
                val photoParts = chunk.mapNotNull { photo ->
                    photo.body
                }

                val metadataPart = prepareMetadataPart(chunk)

                val response = getPhotosUseCase.submitPhoto(albumId, photoParts, metadataPart)
                response.collectLatest { result ->
                    _photoUploadState.value = result
                    if (result.status == ApiStatus.ERROR || result.status == ApiStatus.FAIL) {
                        flag = false
                    }
                }
            }
            if (flag) {
                _albumUiEvent.emit(AlbumUiEvent.FinishPhotoUpload)
                initPhotos()
                initMarkers()
            }
        }
    }

    override fun onClickDeletePhoto() {
        viewModelScope.launch {
            val albumId = _selectedPhotoAlbum.value?.id ?: "-1"
            val photoId = _selectedPhoto.value?.id ?: "-1"
            val response = getPhotosUseCase.deletePhoto(albumId, photoId)
            when ((response.status == ApiStatus.SUCCESS) && (response.data == true)) {
                true -> {
                    _albumUiEvent.emit(AlbumUiEvent.DeletePhotoSuccess)
                    initPhotos()
                }

                false -> {
                    _albumUiEvent.emit(AlbumUiEvent.DeletePhotoFail)
                }
            }
        }
    }

    override fun onClickCommentSubmit() {
        viewModelScope.launch {
            val response = getCommentsUseCase.submitPhotoComment(
                albumId = _selectedPhotoAlbum.value?.id ?: "",
                photoId = _selectedPhoto.value?.id ?: "",
                body = prepareComment(_commentInput.value)
            )
            response.collectLatest { result ->
                _commentSubmitState.value = result
                if (_commentSubmitState.value.status == ApiStatus.SUCCESS) {
                    _commentInput.value = ""
                    _albumUiEvent.emit(AlbumUiEvent.PhotoCommentSubmitSuccess)
                    initComments()
                }
            }
        }
    }

    override fun onClickCommentUpdate(comment: Comment) {
        viewModelScope.launch {
            val response = getCommentsUseCase.modifyPhotoComment(
                albumId = _selectedPhotoAlbum.value?.id ?: "",
                photoId = _selectedPhoto.value?.id ?: "",
                commentID = comment.id,
                body = prepareComment(_commentUpdateInput.value)
            )
            when ((response.status == ApiStatus.SUCCESS) && (response.data == true)) {
                true -> {
                    initComments()
                    _commentUpdateInput.value = ""
                    _albumUiEvent.emit(AlbumUiEvent.PhotoCommentUpdateSuccess)
                }

                else -> {
                    _albumUiEvent.emit(AlbumUiEvent.PhotoCommentUpdateFail)
                }
            }
        }
    }

    override fun onClickCommentDelete(comment: Comment) {
        viewModelScope.launch {
            _selectedPhotoComment.value = comment
            initComments()
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDelete)
        }
    }

    override fun onClickCommentDeleteFinish() {
        viewModelScope.launch {
            _selectedPhotoComment.value?.let { comment ->
                val response = getCommentsUseCase.deletePhotoComment(
                    albumId = _selectedPhotoAlbum.value?.id ?: "",
                    photoId = _selectedPhoto.value?.id ?: "",
                    commentID = comment.id
                )
                when ((response.status == ApiStatus.SUCCESS) && (response.data == true)) {
                    true -> {
                        initComments()
                        _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDeleteFinish)
                    }

                    else -> {
                        _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDeleteFail)
                    }
                }
            }
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDeleteFinish)
        }
    }

    fun initPhotos() {
        viewModelScope.launch {
            _photos.value = emptyList()
            val albumId = _selectedPhotoAlbum.value?.id ?: "-1"
            val response = getPhotosUseCase.getPhotos(albumId)
            when (response.status) {
                ApiStatus.SUCCESS -> {
                    val photos = response.data ?: emptyList()
                    _photos.value = photos
                    _selectedPhotos.value = _photos.value
                    initMarkers()
                    _albumUiEvent.emit(AlbumUiEvent.GoToAlbumDetail)
                }

                else -> {
                    _albumUiEvent.emit(AlbumUiEvent.GetAlbumDetailFail)
                }
            }
        }
    }

    private fun initMarkers() {
        val newMarkers = mutableListOf<MarkerData>()
        _selectedPhotos.value.forEachIndexed { index, photo ->
            newMarkers.add(MarkerData(index + 1, photo, null, false))
        }
        _markers.value = newMarkers.groupBy { it.photo.photoPlace }
            .map { (place, markerList) -> place to markerList }
    }

    private fun validUploadPhotos() {
        if (_uploadPhotos.value.size <= 1) {
            _albumUiState.update { it.copy(photoUploadValidState = EmptyState.EMPTY) }
        } else {
            _albumUiState.update { it.copy(photoUploadValidState = EmptyState.NONE) }
        }
    }

    @SuppressLint("Recycle")
    private fun absolutelyPath(uri: Uri?): String? {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { application.contentResolver.query(uri, proj, null, null, null) }
        cursor?.moveToNext()
        val index = cursor?.getColumnIndex(MediaStore.MediaColumns.DATA)

        return index?.let { cursor.getString(index) }
    }

    private fun fetchGalleryImages(): List<PhotoUploadUiState> {
        val imageList = mutableListOf<PhotoUploadUiState>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val selection =
            "${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} <= ?"
        val startDate = _selectedPhotoAlbum.value?.startDate ?: "1970.01.01"
        val endDate = _selectedPhotoAlbum.value?.endDate ?: "1970.01.01"
        val filterStartDate: Long =
            SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).parse(startDate)?.time ?: 0L
        val filterEndDate: Long = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            .parse(endDate)?.time ?: Long.MAX_VALUE
        val selectionArgs = arrayOf(filterStartDate.toString(), filterEndDate.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val query = application.contentResolver.query(
            queryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.setRequireOriginal(ContentUris.withAppendedId(queryUri, id))
                } else {
                    ContentUris.withAppendedId(queryUri, id)
                }
                val (latitude, longitude) = getLatLngFromExif(uri)
                val dateTaken = cursor.getLong(dateTakenColumn)
                val path = absolutelyPath(uri)
                path?.let {
                    val file = File(path)
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("files", file.name, requestBody)
                    imageList.add(
                        PhotoUploadUiState.UploadedPhoto(
                            uri,
                            body,
                            latitude,
                            longitude,
                            dateTaken
                        )
                    )
                }
            }
        }

        return imageList
    }


    private fun getLatLngFromMediaStore(pickerUri: Uri): Pair<Double, Double> {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
            )

            application.contentResolver.query(
                pickerUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.columnNames.forEach { columnName ->
                        val index = cursor.getColumnIndex(columnName)
                    }

                    val latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                    val longIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE)

                    if (latIndex != -1 && longIndex != -1) {
                        val latitude = cursor.getDouble(latIndex)
                        val longitude = cursor.getDouble(longIndex)
                        return if (latitude != 0.0 || longitude != 0.0) {
                            Pair(latitude, longitude)
                        } else {
                            Pair(0.0, 0.0)
                        }
                    } else {
                        Pair(0.0, 0.0)
                    }
                }
            }

            application.contentResolver.openInputStream(pickerUri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val latLong = exif.latLong
                if (latLong != null) {
                    return Pair(latLong[0], latLong[1])
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Pair(0.0, 0.0)
    }

    /*** EXIF에서 위도, 경도 정보를 가져오는 메소드 ***/
    private fun getLatLngFromExif(uri: Uri?): Pair<Double, Double> {
        if (uri?.toString()?.contains("com.android.providers.media.photopicker") == true) {
            val mediaStoreResult = getLatLngFromMediaStore(uri)
            if (mediaStoreResult != Pair(0.0, 0.0)) {
                return mediaStoreResult
            }
        }

        try {
            uri?.let {
                val inputStream = application.contentResolver.openInputStream(uri)
                inputStream?.use {
                    val exif = ExifInterface(it)

                    val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.let { latitude ->
                        convertToDegree(latitude)
                    }
                    val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.let { longitude ->
                        convertToDegree(longitude)
                    }

                    if (lat != null && lng != null) {
                        val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
                        val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

                        val adjustedLat = if (latRef == "S") -lat else lat
                        val adjustedLon = if (lonRef == "W") -lng else lng

                        return Pair(adjustedLat, adjustedLon)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(0.0, 0.0)
    }

    private fun prepareMetadataPart(photoEntities: List<PhotoEntity>): RequestBody {
        val metadataList = photoEntities.map {
            mapOf(
                "place" to it.place,
                "latitude" to it.latitude,
                "longitude" to it.longitude,
                "takenAt" to it.takenAt,
                "albumId" to _selectedPhotoAlbum.value?.id,
                "userId" to _userId.value,
                "filePath" to "",
            )
        }
        val json = Gson().toJson(mapOf("photos" to metadataList))

        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    private fun initTempMarkers() {
        val newTempMarkers = mutableListOf<MarkerData>()
        _selectedPhotos.value.forEachIndexed { index, photo ->
            newTempMarkers.add(MarkerData(index + 1, photo, null, false))
        }
        var index = _selectedPhotos.value.size + 1
        for (i in 1 until _uploadPhotos.value.size) {
            val uploadPhoto = _uploadPhotos.value[i] as PhotoUploadUiState.UploadedPhoto
            val photo = Photo(
                index.toString(),
                _selectedPhotoAlbum.value?.id ?: "",
                "",
                "",
                uploadPhoto.photoUri.toString(),
                uploadPhoto.latitude,
                uploadPhoto.longitude,
                formatLongToDateTime(uploadPhoto.takenAt),
                ""
            )
            newTempMarkers.add(MarkerData(index, photo, uploadPhoto.body, true))
            index++
        }
        _tempPhotos.value = newTempMarkers
        Timber.d("Marker: ${_tempPhotos.value}")
    }

    /*** 로직 수정이 필요함 ***/
    private fun getImageIdFromUri(uri: Uri): String {
        val tempId = uri.toString().split("/")
        return tempId.last()
    }

    private fun getLatLngFromPhotos() {
        _uploadPhotos.value.forEach { photo ->
            if (photo is PhotoUploadUiState.UploadedPhoto) {
                Timber.d("Lat: ${photo.latitude}, Lng: ${photo.longitude}")
            }
        }
    }

    private fun updatePhotoPlace() {
        val nowTempPlaces = _newMarkers.value.toMutableList()
        val newTempPlaces = mutableListOf<Pair<String, List<MarkerData>>>()
        var flag = false
        val photo =
            nowTempPlaces[_tempClassifiedPhotoIndex.value.first].second[_tempClassifiedPhotoIndex.value.second]
        nowTempPlaces.forEachIndexed { index, pair ->
            val placeName = pair.first
            val newPhotos = pair.second.toMutableList()
            if (placeName == _tempNewPlaceName.value) {
                flag = true
                newPhotos.add(photo)
            }
            if (index == _tempClassifiedPhotoIndex.value.first) {
                newPhotos.removeAt(_tempClassifiedPhotoIndex.value.second)
            }
            if (newPhotos.isNotEmpty()) {
                newTempPlaces.add(Pair(pair.first, newPhotos))
            }
        }
        if (!flag) {
            newTempPlaces.add(Pair(_tempNewPlaceName.value, listOf(photo)))
        }

        _newMarkers.value = newTempPlaces
        _tempClassifiedPhotoIndex.value = Pair(-1, -1)
    }

    private fun prepareComment(content: String): RequestBody {
        val metadata = mapOf("content" to content)
        val json = Gson().toJson(metadata)

        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }

    fun initAlbums() {
        viewModelScope.launch {
            _photoAlbums.value = emptyList()
            if (getUserInfoUseCase.getJWT().first() == null) {
                _albumUiEvent.emit(AlbumUiEvent.GetAlbumsFail)
                return@launch
            }

            val response = getAlbumsUseCase.getAlbums()
            response.collectLatest { result ->
                _albumsState.value = result
                if (_albumsState.value.status == ApiStatus.SUCCESS) {
                    val albums = _albumsState.value.data ?: emptyList()
                    val newAlbums = mutableListOf<PhotoAlbum>()
                    albums.forEach { album ->
                        newAlbums.add(album)
                    }
                    _photoAlbums.value = newAlbums
                    _albumUiEvent.emit(AlbumUiEvent.GetAlbumsSuccess)
                }
            }
        }
    }

    fun initPhotoPlaces() {
        val newPhotoPlaces = mutableListOf<PhotoPlace>()

        _selectedPhotoAlbum.value?.let { album ->
            newPhotoPlaces.add(PhotoPlace("0", album.id, "전체", true))
            _markers.value.forEachIndexed { index, marker ->
                newPhotoPlaces.add(
                    PhotoPlace(
                        (index + 1).toString(),
                        album.id,
                        marker.first,
                        false
                    )
                )
            }
        }

        _photoPlaces.value = newPhotoPlaces
    }

    fun initComments() {
        viewModelScope.launch {
            val albumId = _selectedPhotoAlbum.value?.id ?: "-1"
            val photoId = _selectedPhoto.value?.id ?: "-1"
            val response = getCommentsUseCase.getPhotoComments(albumId, photoId)
            when (response.status) {
                ApiStatus.SUCCESS -> {
                    val comments = response.data ?: emptyList()
                    _selectedPhotoComments.value = comments
                }

                else -> {
                    _albumUiEvent.emit(AlbumUiEvent.GetPhotoCommentsFail)
                }
            }
        }
    }

    fun setPhotoPlace(index: Int) {
        _selectedPhotoPlace.value = _photoPlaces.value[index]
    }

    fun validPhotoComment(comment: CharSequence) {
        when (comment.isBlank()) {
            true -> _albumUiState.update { it.copy(commentValidState = InputValidState.NONE) }

            else -> _albumUiState.update { it.copy(commentValidState = InputValidState.VALID) }
        }
    }

    fun selectedPlace(placeName: String) {
        when (placeName == "직접 입력") {
            true -> {
                _tempNewPlaceName.value = ""
                _albumUiState.update { it.copy(enterDirectlyState = EnterState.VALID) }
            }

            else -> {
                _tempNewPlaceName.value = placeName
                _albumUiState.update { it.copy(enterDirectlyState = EnterState.NONE) }
            }
        }
    }

    fun validUpdatePlaceName(placeName: CharSequence) {
        when (placeName.isBlank()) {
            true -> _albumUiState.update { it.copy(updatePlaceNameState = InputValidState.NONE) }

            else -> _albumUiState.update { it.copy(updatePlaceNameState = InputValidState.VALID) }
        }
    }

    fun validNewPlaceName(placeName: CharSequence) {
        when (placeName.isBlank()) {
            true -> _albumUiState.update { it.copy(newPlaceNameState = InputValidState.NONE) }

            else -> _albumUiState.update { it.copy(newPlaceNameState = InputValidState.VALID) }
        }
    }

    fun initUploadPhotos() {
        viewModelScope.launch {
            _uploadPhotos.value = listOf(PhotoUploadUiState.PhotoUploadButton())
            val newPhotos = fetchGalleryImages()
            Timber.d("initUploadPhotos : ${newPhotos}")
            _uploadPhotos.value += newPhotos
            initTempMarkers()
            getLatLngFromPhotos()
            validUploadPhotos()
        }
    }

    fun addUploadPhoto(uri: Uri?, takenAt: Long, body: MultipartBody.Part) {
        if (uri == null) return
        viewModelScope.launch {
            val idFromUi = getImageIdFromUri(uri)
            val newPhotos = _uploadPhotos.value.toMutableList()
            for (i in 1 until (newPhotos.size)) {
                val photoUploadUiState = (newPhotos[i] as PhotoUploadUiState.UploadedPhoto)
                val nowIDFromUi = getImageIdFromUri(photoUploadUiState.photoUri)
                if (nowIDFromUi == idFromUi) {
                    _albumUiEvent.emit(AlbumUiEvent.PhotoDuplicated)
                    return@launch
                }
            }
            val isPickerUri = uri.toString().contains("com.android.providers.media.photopicker")
            val originalUri = if (!isPickerUri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    MediaStore.setRequireOriginal(uri)
                } catch (e: Exception) {
                    uri
                }
            } else {
                uri
            }
            val (latitude, longitude) = getLatLngFromExif(originalUri)
            newPhotos.add(
                1,
                PhotoUploadUiState.UploadedPhoto(originalUri, body, latitude, longitude, takenAt)
            )
            _uploadPhotos.value = newPhotos
            initTempMarkers()
            getLatLngFromPhotos()
            validUploadPhotos()
        }
    }

    fun addUploadPhoto(
        uri: Uri?,
        takenAt: Long,
        body: MultipartBody.Part,
        latitude: Double,
        longitude: Double
    ) {
        if (uri == null) return
        viewModelScope.launch {
            val idFromUi = getImageIdFromUri(uri)
            val newPhotos = _uploadPhotos.value.toMutableList()
            for (i in 1 until (newPhotos.size)) {
                val photoUploadUiState = (newPhotos[i] as PhotoUploadUiState.UploadedPhoto)
                val nowIDFromUi = getImageIdFromUri(photoUploadUiState.photoUri)
                if (nowIDFromUi == idFromUi) {
                    _albumUiEvent.emit(AlbumUiEvent.PhotoDuplicated)
                    return@launch
                }
            }
            val isPickerUri = uri.toString().contains("com.android.providers.media.photopicker")
            val originalUri = if (!isPickerUri && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    MediaStore.setRequireOriginal(uri)
                } catch (e: Exception) {
                    uri
                }
            } else {
                uri
            }
            newPhotos.add(
                1,
                PhotoUploadUiState.UploadedPhoto(originalUri, body, latitude, longitude, takenAt)
            )
            _uploadPhotos.value = newPhotos
            initTempMarkers()
            getLatLngFromPhotos()
            validUploadPhotos()
        }
    }

    fun initNewMarkers(tags: List<String>, markers: List<List<MarkerData>>) {
        val newNewMarkers = mutableListOf<Pair<String, List<MarkerData>>>()
        tags.forEachIndexed { index, tag ->
            if (markers[index].isNotEmpty()) {
                newNewMarkers.add(Pair(tag, markers[index]))
            }
        }

        _newMarkers.value = newNewMarkers.toList()
    }

    fun deleteUploadPhoto(index: Int) {
        val newPhotos = _uploadPhotos.value.toMutableList()
        newPhotos.removeAt(index)
        _uploadPhotos.value = newPhotos
        initTempMarkers()
        validUploadPhotos()
    }

    fun updatePlaceName(position: Int) {
        viewModelScope.launch {
            _updatePlaceIndex.value = position
            _updatePlaceName.value = _newMarkers.value[position].first
            _albumUiEvent.emit(AlbumUiEvent.UpdatePhotoPlaceName)
        }
    }

    fun fetchUserId(): Flow<String?> = flow {
        val id = getUserInfoUseCase.getUserId().first()
        emit(id)
    }

    fun downloadImage() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.PhotoDownload)

            // filePath에서 확장자 추출
            val fileExtension = _selectedPhoto.value?.filePath?.substringAfterLast(".", "")
                ?: "jpg"  // 기본값으로 jpg 설정

            // 저장할 파일명에 확장자 추가
            val fileName = "${_selectedPhoto.value?.photoPlace}.${fileExtension}"

            val request = DownloadManager.Request(Uri.parse(_selectedPhoto.value?.filePath)).apply {
                setTitle("이미지 다운로드")
                setDescription("이미지를 다운로드하는 중...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }

            val downloadManager =
                application.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }
    }
}