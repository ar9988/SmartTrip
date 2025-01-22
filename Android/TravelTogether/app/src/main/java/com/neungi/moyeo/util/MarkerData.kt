package com.neungi.moyeo.util

import com.naver.maps.geometry.LatLng
import com.naver.maps.map.clustering.ClusteringKey

data class MarkerData(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val profile: String
) : ClusteringKey {

    override fun getPosition(): LatLng = LatLng(latitude, longitude)
}