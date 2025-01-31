package com.neungi.moyeo.util

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusteringKey
import com.neungi.domain.model.Photo

data class MarkerData(
    val id: Int,
    val photo: Photo,
    val isVisible: Boolean
) : ClusteringKey {

    override fun getPosition(): LatLng = LatLng(photo.latitude, photo.longitude)
}