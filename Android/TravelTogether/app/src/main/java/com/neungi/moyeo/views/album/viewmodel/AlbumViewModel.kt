package com.neungi.moyeo.views.album.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.naver.maps.geometry.LatLng
import com.neungi.domain.model.Comment
import com.neungi.domain.model.Photo
import com.neungi.domain.model.PhotoAlbum
import com.neungi.domain.model.PhotoPlace
import com.neungi.domain.usecase.GetAlbumsUseCase
import com.neungi.domain.usecase.GetCommentsUseCase
import com.neungi.domain.usecase.GetPhotoLocationsUseCase
import com.neungi.domain.usecase.GetPhotosUseCase
import com.neungi.moyeo.util.CommonUtils.convertToDegree
import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.util.InputValidState
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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
    private val getPhotosUseCase: GetPhotosUseCase
) : AndroidViewModel(application), OnAlbumClickListener {

    /****** UiState, UiEvent ******/
    private val _albumUiState = MutableStateFlow<AlbumUiState>(AlbumUiState())
    val albumUiState = _albumUiState.asStateFlow()

    private val _albumUiEvent = MutableSharedFlow<AlbumUiEvent>()
    val albumUiEvent = _albumUiEvent.asSharedFlow()

    /****** Datas ******/
    private val _photoAlbums = MutableStateFlow<List<PhotoAlbum>>(emptyList())
    val photoAlbums = _photoAlbums.asStateFlow()

    private val _locations = MutableStateFlow<List<LatLng>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _markers = MutableStateFlow<List<MarkerData>>(emptyList())
    val markers = _markers.asStateFlow()

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
        initTempAlbums()
    }

    override fun onClickAlbum(photoAlbum: PhotoAlbum) {
        viewModelScope.launch {
            _selectedPhotoAlbum.value = photoAlbum
            initTempPhotos() // 추후 Album ID로 Photo를 전부 가져오는 비즈니스 로직 작성
            _albumUiEvent.emit(AlbumUiEvent.GoToAlbumDetail)
        }
    }

    override fun onClickBackToAlbum() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.BackToAlbum)
        }
    }

    override fun onClickPhotoPlace(photoPlace: PhotoPlace) {
        viewModelScope.launch {
            val newPhotoPlaces = mutableListOf<PhotoPlace>()
            when (photoPlace.name == "전체") {
                true -> _selectedPhotoPlace.value = null

                else -> _selectedPhotoPlace.value = photoPlace
            }

            val place = _selectedPhotoPlace.value
            when (place == null) {
                true -> {
                    Timber.d("전체 이미지")
                    _selectedPhotos.value = _photos.value
                    _photoPlaces.value.forEach {
                        when (it.name == "전체") {
                            true -> {
                                newPhotoPlaces.add(PhotoPlace(it.id, it.albumId, it.name, true))
                            }

                            else -> {
                                newPhotoPlaces.add(PhotoPlace(it.id, it.albumId, it.name, false))
                            }
                        }
                    }
                }

                false -> {
                    val placesIndices = place.name.split(",")
                    _selectedPhotos.value = _photos.value.filter { placesIndices.contains(it.id) }
                    _photoPlaces.value.forEach {
                        when (it.name == place.name) {
                            true -> {
                                newPhotoPlaces.add(PhotoPlace(it.id, it.albumId, it.name, true))
                            }

                            else -> {
                                newPhotoPlaces.add(PhotoPlace(it.id, it.albumId, it.name, false))
                            }
                        }
                    }
                }
            }

            _photoPlaces.value = newPhotoPlaces.toList()
            _albumUiEvent.emit(AlbumUiEvent.SelectPlace)
        }
    }

    override fun onClickPhoto(photo: Photo) {
        viewModelScope.launch {
            _selectedPhoto.value = photo
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

    override fun onClickFinishPhotoUpload() {
        viewModelScope.launch {
            _albumUiEvent.emit(AlbumUiEvent.FinishPhotoUpload)
        }
    }

    override fun onClickCommentSubmit() {
        viewModelScope.launch {
            submitComment()
            getComments()
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentSubmit)
        }
    }

    override fun onClickCommentUpdate(comment: Comment) {
        viewModelScope.launch {
            updateComment(comment)
            getComments()
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentUpdate)
        }
    }

    override fun onClickCommentDelete(comment: Comment) {
        viewModelScope.launch {
            _selectedPhotoComment.value = comment
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDelete)
        }
    }

    override fun onClickCommentDeleteFinish() {
        viewModelScope.launch {
            deleteComment()
            _albumUiEvent.emit(AlbumUiEvent.PhotoCommentDeleteFinish)
        }
    }

    private fun initTempAlbums() {
        val newAlbums = mutableListOf<PhotoAlbum>()
        newAlbums.add(
            PhotoAlbum(
                "1",
                "1",
                "18제주팟",
                "https://cdn.hkbs.co.kr/news/photo/202405/755302_490954_5034.jpg",
                "2025.01.01",
                "2025.01.31"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "2",
                "2",
                "19제주팟",
                "https://t1.daumcdn.net/thumb/R720x0/?fname=http://t1.daumcdn.net/brunch/service/user/3fuW/image/oKAZIY6tS8e4z_7r4oOgDS-BPgU.jpg",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "3",
                "3",
                "20제주팟",
                "https://img.freepik.com/free-photo/tourist-with-map-sunny-sky-background_23-2147828103.jpg",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "3",
                "3",
                "21제주팟",
                "https://content.skyscnr.com/m/26448d8c5b60885d/original/eyeem_141769102-jpg.jpg?resize=1224%3Aauto",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "3",
                "3",
                "22제주팟",
                "https://img.modetour.com/eagle/photoimg/33769/bfile/636529163406869782.png?resize=y&resize_w=603&resize_h=360&w_h_fill=y",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "3",
                "3",
                "23제주팟",
                "https://cdn.informaticsview.com/news/photo/202410/647_2527_2618.jpg",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(
                "3",
                "3",
                "24제주팟",
                "https://dimg.donga.com/wps/NEWS/IMAGE/2019/01/02/93531867.2.jpg",
                "2025-01-17",
                "2025-01-27"
            )
        )
        newAlbums.add(
            PhotoAlbum(

                "3",
                "3",
                "25제주팟",
                "https://cdn.drtour.com/MainDrtour/item/2025/1/67e09f44-6c78-4c0f-91aa-87b0fdf66f18.jpg",
                "2025-01-17",
                "2025-01-18"
            )
        )
        _photoAlbums.value = newAlbums
    }

    private fun initTempPhotos() {
        val newPhotos = mutableListOf<Photo>()
        _selectedPhotoAlbum.value?.let { album ->
            newPhotos.add(
                Photo(
                    "1",
                    album.id,
                    "장소1",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.106647982205345,
                    128.4179970752263,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "2",
                    album.id,
                    "장소2",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.10671844993927,
                    128.4185147185645,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "3",
                    album.id,
                    "장소3",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.10597868662755,
                    128.41782402493536,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "4",
                    album.id,
                    "장소4",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.1048985483351,
                    128.42000332554514,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "5",
                    album.id,
                    "장소5",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.10459269177041,
                    128.4191982908834,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "6",
                    album.id,
                    "장소6",
                    "김싸피",
                    "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420",
                    36.10386391426613,
                    128.41986255304064,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "7",
                    album.id,
                    "장소7",
                    "김싸피",
                    "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083",
                    36.10665000270314,
                    128.42276664905924,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "8",
                    album.id,
                    "장소8",
                    "김싸피",
                    "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083",
                    36.10642775378698,
                    128.42175210127704,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "9",
                    album.id,
                    "장소9",
                    "김싸피",
                    "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083",
                    36.10674602946057,
                    128.42226866187877,
                    "25.01.17 12:00",
                    ""
                )
            )
            newPhotos.add(
                Photo(
                    "10",
                    album.id,
                    "장소10",
                    "김싸피",
                    "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083",
                    36.10722862269554,
                    128.42336564725466,
                    "25.01.17 12:00",
                    ""
                )
            )
        }
        _photos.value = newPhotos
        _selectedPhotos.value = _photos.value

        val newMarkers = mutableListOf<MarkerData>()
        _selectedPhotos.value.forEachIndexed { index, photo ->
            newMarkers.add(MarkerData(index + 1, photo, true))
        }
        _markers.value = newMarkers.toList()

        val newPhotoComments = mutableListOf<Comment>()

        newPhotoComments.add(Comment("1", "1", "김싸피", "굿굿", "25.01.17 13:00", "25.01.17 13:00"))
        newPhotoComments.add(Comment("2", "1", "이싸피", "짱~", "25.01.17 13:30", "25.01.17 13:00"))
        newPhotoComments.add(Comment("3", "1", "정싸피", "좋아요", "25.01.17 14:00", "25.01.17 13:00"))

        _selectedPhotoComments.value = newPhotoComments
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
                val uri = ContentUris.withAppendedId(queryUri, id)
                val (latitude, longitude) = getLatLngFromExif(uri)
                val dateTaken = cursor.getLong(dateTakenColumn)
                val path = absolutelyPath(uri)
                path?.let {
                    val file = File(path)
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    imageList.add(PhotoUploadUiState.UploadedPhoto(uri, body, latitude, longitude, dateTaken))
                }

            }
        }

        return imageList
    }

    /*** EXIF에서 위도, 경도 정보를 가져오는 메소드 ***/
    private fun getLatLngFromExif(uri: Uri?): Pair<Double, Double> {
        try {
            uri?.let {
                val inputStream: InputStream? = application.contentResolver.openInputStream(uri)
                inputStream?.use {
                    val exif = ExifInterface(it)

                    val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)?.let { latitude ->
                        convertToDegree(latitude)
                    }
                    val lng = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)?.let { longitude ->
                        convertToDegree(longitude)
                    }

                    Timber.d("lat: $lat, lng: $lng")

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

    private suspend fun getComments() {
        getCommentsUseCase.getPhotoComments(
            albumId = _selectedPhotoAlbum.value?.id ?: "",
            photoId = _selectedPhoto.value?.id ?: ""
        )
    }

    private suspend fun submitComment() {
        getCommentsUseCase.submitPhotoComment(
            albumId = _selectedPhotoAlbum.value?.id ?: "",
            photoId = _selectedPhoto.value?.id ?: "",
            body = Comment(
                id = "",
                photoId = _selectedPhoto.value?.id ?: "",
                author = "",
                content = _commentInput.value,
                createdAt = "",
                updatedAt = ""
            )
        )
    }

    private suspend fun updateComment(comment: Comment) {
        getCommentsUseCase.modifyPhotoComment(
            albumId = _selectedPhotoAlbum.value?.id ?: "",
            photoId = _selectedPhoto.value?.id ?: "",
            commentID = comment.id,
            body = comment
        )
    }

    private suspend fun deleteComment() {
        _selectedPhotoComment.value?.let { comment ->
            getCommentsUseCase.deletePhotoComment(
                albumId = _selectedPhotoAlbum.value?.id ?: "",
                photoId = _selectedPhoto.value?.id ?: "",
                commentID = comment.id,
                body = comment
            )
            getComments()
        }
    }

    fun initPhotoPlaces(tags: List<String>) {
        val newPhotoPlaces = mutableListOf<PhotoPlace>()

        _selectedPhotoAlbum.value?.let { album ->
            newPhotoPlaces.add(PhotoPlace("0", album.id, "전체", true))
            tags.forEachIndexed { index, tag ->
                newPhotoPlaces.add(PhotoPlace((index + 1).toString(), album.id, tag, false))
            }
        }

        _photoPlaces.value = newPhotoPlaces
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

    fun initUploadPhotos() {
        viewModelScope.launch {
            _uploadPhotos.value = listOf(PhotoUploadUiState.PhotoUploadButton())
            val newPhotos = fetchGalleryImages()
            _uploadPhotos.value += newPhotos
            getLatLngFromPhotos()
            validUploadPhotos()
        }
    }

    fun addUploadPhoto(uri: Uri?, takenAt: Long, body: MultipartBody.Part) {
        if (uri == null) return
        viewModelScope.launch {
            val idFromUi = getImageIdFromUri(uri)
            Timber.d("Id: $idFromUi")
            val newPhotos = _uploadPhotos.value.toMutableList()
            for (i in 1 until(newPhotos.size)) {
                val photoUploadUiState = (newPhotos[i] as PhotoUploadUiState.UploadedPhoto)
                val nowIDFromUi = getImageIdFromUri(photoUploadUiState.photoUri)
                Timber.d("Now Id: $nowIDFromUi")
                if (nowIDFromUi == idFromUi) {
                    _albumUiEvent.emit(AlbumUiEvent.PhotoDuplicated)
                    return@launch
                }
            }
            val (latitude, longitude) = getLatLngFromExif(uri)
            newPhotos.add(1, PhotoUploadUiState.UploadedPhoto(uri, body, latitude, longitude, takenAt))
            _uploadPhotos.value = newPhotos
            getLatLngFromPhotos()
            validUploadPhotos()
        }
    }

    fun deleteUploadPhoto(index: Int) {
        val newPhotos = _uploadPhotos.value.toMutableList()
        newPhotos.removeAt(index)
        _uploadPhotos.value = newPhotos
        validUploadPhotos()
    }
}