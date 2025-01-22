package com.neungi.moyeo.util

import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object MapUtil {

    suspend fun makeMarker(
        marker: List<MarkerData>,
        builder: Clusterer.ComplexBuilder<MarkerData>
    ): Pair<Clusterer<MarkerData>, List<String>> {
        val newTags = mutableListOf<String>()
        val cluster: Clusterer<MarkerData> = builder.tagMergeStrategy { cluster ->
            cluster.children.map { it.tag }.joinToString(",")
        }
            .apply {
                clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {

                    override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                        super.updateClusterMarker(info, marker)

                        Timber.d("Tag: ${info.tag as String}")
                        newTags.add(info.tag.toString())

                        marker.onClickListener = Overlay.OnClickListener {
                            true
                        }
                    }
                })
            }
            .build()

        withContext(Dispatchers.Default) {
            marker.forEach { item ->
                cluster.add(item, "${item.id}")
            }
        }

        return Pair(cluster, newTags.toList())
    }

    suspend fun deleteMarker(marker: Clusterer<MarkerData>) {
        withContext(Dispatchers.Default) {
            marker.map = null
        }
    }

    fun clickMarker(
        builder: Clusterer.ComplexBuilder<MarkerData>,
        markerInfo: (MarkerData) -> Unit?,
        clusterTag: (List<Int>, Double, Double) -> Unit?
    ) {
        builder.clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {

            override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {
                super.updateClusterMarker(info, marker)

                marker.onClickListener = Overlay.OnClickListener {
                    val idList = (info.tag as String).split(",").map { it.toInt() }
                    clusterTag(idList, marker.position.latitude, marker.position.longitude)
                    false
                }
            }
        }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {

            override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                super.updateLeafMarker(info, marker)

                marker.onClickListener = Overlay.OnClickListener {
                    val markerData = info.key as MarkerData
                    markerInfo(markerData)
                    true
                }
            }
        })
    }
}