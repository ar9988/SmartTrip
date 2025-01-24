package com.neungi.moyeo.views.album.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
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
import com.neungi.moyeo.util.EmptyState
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private val _uploadPhotos = MutableStateFlow<List<PhotoUploadUiState>>(emptyList())
    val uploadPhotos = _uploadPhotos.asStateFlow()

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

    private fun initTempAlbums() {
        val newAlbums = mutableListOf<PhotoAlbum>()
        newAlbums.add(PhotoAlbum("1", "1", "18제주팟", "https://cdn.hkbs.co.kr/news/photo/202405/755302_490954_5034.jpg", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("2", "2", "19제주팟", "https://t1.daumcdn.net/thumb/R720x0/?fname=http://t1.daumcdn.net/brunch/service/user/3fuW/image/oKAZIY6tS8e4z_7r4oOgDS-BPgU.jpg", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "20제주팟", "https://img.freepik.com/free-photo/tourist-with-map-sunny-sky-background_23-2147828103.jpg", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "21제주팟", "https://content.skyscnr.com/m/26448d8c5b60885d/original/eyeem_141769102-jpg.jpg?resize=1224%3Aauto", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "22제주팟", "https://img.modetour.com/eagle/photoimg/33769/bfile/636529163406869782.png?resize=y&resize_w=603&resize_h=360&w_h_fill=y", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "23제주팟", "https://cdn.informaticsview.com/news/photo/202410/647_2527_2618.jpg", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "24제주팟", "https://dimg.donga.com/wps/NEWS/IMAGE/2019/01/02/93531867.2.jpg", "25.01.17", "25.01.20"))
        newAlbums.add(PhotoAlbum("3", "3", "25제주팟", "https://cdn.drtour.com/MainDrtour/item/2025/1/67e09f44-6c78-4c0f-91aa-87b0fdf66f18.jpg", "25.01.17", "25.01.20"))
        _photoAlbums.value = newAlbums
    }

    private fun initTempPhotos() {
        val newPhotos = mutableListOf<Photo>()
        _selectedPhotoAlbum.value?.let { album ->
            newPhotos.add(Photo("1", album.id, "장소1", "김싸피", "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.106647982205345, 128.4179970752263, "", ""))
            newPhotos.add(Photo("2", album.id, "장소2", "김싸피","https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.10671844993927, 128.4185147185645, "", ""))
            newPhotos.add(Photo("3", album.id, "장소3", "김싸피", "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.10597868662755, 128.41782402493536, "", ""))
            newPhotos.add(Photo("4", album.id, "장소4", "김싸피", "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.1048985483351, 128.42000332554514, "", ""))
            newPhotos.add(Photo("5", album.id, "장소5", "김싸피", "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.10459269177041, 128.4191982908834, "", ""))
            newPhotos.add(Photo("6", album.id, "장소6", "김싸피", "https://mblogthumb-phinf.pstatic.net/20130925_10/2mcool_1380077202055F5nIu_JPEG/%C0%CE%B5%BF%C3%CA01.jpg?type=w420", 36.10386391426613, 128.41986255304064, "", ""))
            newPhotos.add(Photo("7", album.id, "장소7", "김싸피", "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083", 36.10665000270314, 128.42276664905924, "", ""))
            newPhotos.add(Photo("8", album.id, "장소8", "김싸피", "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083", 36.10642775378698, 128.42175210127704, "", ""))
            newPhotos.add(Photo("9", album.id, "장소9", "김싸피", "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083", 36.10674602946057, 128.42226866187877, "", ""))
            newPhotos.add(Photo("10", album.id, "장소10", "김싸피", "https://gumi.grandculture.net/Image?localName=gumi&id=GC012P1083", 36.10722862269554, 128.42336564725466, "", ""))
        }
        _photos.value = newPhotos
        _selectedPhotos.value = _photos.value

        val newMarkers = mutableListOf<MarkerData>()
        _selectedPhotos.value.forEachIndexed { index, photo ->
            newMarkers.add(MarkerData(index + 1, photo, true))
        }
        _markers.value = newMarkers.toList()
    }

    private fun fetchGalleryImages(): List<PhotoUploadUiState> {
        val imageList = mutableListOf<PhotoUploadUiState>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val query = application.contentResolver.query(
            queryUri,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(queryUri, id)
                val dateTaken = cursor.getLong(dateTakenColumn)

                imageList.add(PhotoUploadUiState.UploadedPhoto(uri, dateTaken))
            }
        }

        return imageList
    }

    fun initPhotoPlaces(tags: List<String>) {
        val newPhotoPlaces = mutableListOf<PhotoPlace>()

        _selectedPhotoAlbum.value?.let { album ->
            newPhotoPlaces.add(PhotoPlace("0", album.id, "전체", true))
            tags.forEachIndexed { index, tag ->
                newPhotoPlaces.add(PhotoPlace((index + 1).toString(), album.id, tag, false))
            }
        }

        Timber.d("Places: $newPhotoPlaces")
        _photoPlaces.value = newPhotoPlaces
    }

    fun initUploadPhotos() {
        viewModelScope.launch {
            _uploadPhotos.value = listOf(PhotoUploadUiState.PhotoUploadButton())
            val newPhotos = fetchGalleryImages()
            if (newPhotos.isEmpty()) {
                _albumUiState.update { it.copy(photoUploadValidState = EmptyState.EMPTY) }
            } else {
                _albumUiState.update { it.copy(photoUploadValidState = EmptyState.NONE) }
            }
            _uploadPhotos.value += newPhotos
        }
    }

    fun addUploadPhoto(uri: Uri?, takenAt: Long) {
        if (uri == null) return
        val newPhotos = _uploadPhotos.value.toMutableList()
        newPhotos.add(PhotoUploadUiState.UploadedPhoto(uri, takenAt))
        _uploadPhotos.value = newPhotos
    }
}