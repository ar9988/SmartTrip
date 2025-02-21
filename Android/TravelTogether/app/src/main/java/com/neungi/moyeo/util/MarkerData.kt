package com.neungi.moyeo.util

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusteringKey
import com.neungi.domain.model.Photo
import okhttp3.MultipartBody

data class MarkerData(
    val id: Int,
    val photo: Photo,
    val body: MultipartBody.Part?,
    val isNewPhoto: Boolean
) : ClusteringKey {

    override fun getPosition(): LatLng = LatLng(photo.latitude, photo.longitude)
}