package com.neungi.moyeo.views.album.viewmodel

import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng
import com.neungi.domain.usecase.GetAlbumsUseCase
import com.neungi.domain.usecase.GetCommentsUseCase
import com.neungi.domain.usecase.GetPhotoLocationsUseCase
import com.neungi.domain.usecase.GetPhotosUseCase
import com.neungi.moyeo.util.MarkerData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val getAlbumsUseCase: GetAlbumsUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getPhotoLocationsUseCase: GetPhotoLocationsUseCase,
    private val getPhotosUseCase: GetPhotosUseCase
) : ViewModel() {

    private val _albumUiState = MutableStateFlow<AlbumUiState>(AlbumUiState())
    val albumUiState = _albumUiState.asStateFlow()

    private val _albumUiEvent = MutableSharedFlow<AlbumUiEvent>()
    val albumUiEvent = _albumUiEvent.asSharedFlow()

    private val _locations = MutableStateFlow<List<LatLng>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _markers = MutableStateFlow<List<MarkerData>>(emptyList())
    val markers = _markers.asStateFlow()

    init {
        val newMarkers = mutableListOf<MarkerData>()
        newMarkers.add(MarkerData(1, 36.106647982205345, 128.4179970752263, "장소1", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(2, 36.10671844993927, 128.4185147185645, "장소2", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(3, 36.10597868662755, 128.41782402493536, "장소3", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(4, 36.1048985483351, 128.42000332554514, "장소4", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(5, 36.10459269177041, 128.4191982908834, "장소5", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(6, 36.10386391426613, 128.41986255304064, "장소6", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(7, 36.10665000270314, 128.42276664905924, "장소7", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(8, 36.10642775378698, 128.42175210127704, "장소8", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(9, 36.10674602946057, 128.42226866187877, "장소9", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        newMarkers.add(MarkerData(10, 36.10722862269554, 128.42336564725466, "장소10", "https://thegodofsports.com/wp-content/uploads/2022/12/1230411343-scaled-1-1300x867.jpeg"))
        _markers.value = newMarkers.toList()
    }
}