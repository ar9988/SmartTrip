package com.neungi.moyeo.views.album

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.clustering.ClusterMarkerInfo
import com.naver.maps.map.clustering.Clusterer
import com.naver.maps.map.clustering.DefaultClusterMarkerUpdater
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAlbumDetailBinding
import com.neungi.moyeo.util.MarkerData
import com.neungi.moyeo.util.Permissions
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.album.adapter.PhotoAdapter
import com.neungi.moyeo.views.album.adapter.PhotoPlaceAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class AlbumDetailFragment :
    BaseFragment<FragmentAlbumDetailBinding>(R.layout.fragment_album_detail), OnMapReadyCallback {

    private val viewModel: AlbumViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private lateinit var clusterer: Clusterer<MarkerData>
    private lateinit var markerBuilder: Clusterer.ComplexBuilder<MarkerData>
    private val tags = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initFusedLocationClient()
        initRecyclerView()

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initNaverMap()
            } else {
                Toast.makeText(requireContext(), "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isLocationButtonEnabled = true

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    naverMap.locationOverlay.run {
                        isVisible = true
                        position = LatLng(it.latitude, it.longitude)
                    }

                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                    naverMap.moveCamera(cameraUpdate)
                    setMapToFitAllMarkers(viewModel.locations.value)
                    initClusterer()
                }
            }
    }

    private fun initFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!hasPermission()) {
            requestLocationPermission()
        } else {
            initNaverMap()
        }
    }

    private fun hasPermission(): Boolean {
        for (permission in Permissions.LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun initNaverMap() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun setMapToFitAllMarkers(markers: List<LatLng>) {
        if (markers.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        markers.forEach { boundsBuilder.include(it) }
        val bounds = boundsBuilder.build()

        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun initClusterer() {
        markerBuilder = Clusterer.ComplexBuilder<MarkerData>()
        lifecycleScope.launch {
            tags.clear()
            val newClusterer = makeMarker(
                viewModel.markers.value,
                markerBuilder
            ) {
                val newMarkers = mutableListOf<List<MarkerData>>()
                tags.forEach { tag ->
                    val newLocations = mutableListOf<MarkerData>()
                    tag.split(",").forEach { id ->
                        newLocations.add(viewModel.markers.value[id.toInt() - 1])
                    }
                    calculateRepresentativeCoordinate(newLocations)
                    newMarkers.add(newLocations)
                    Timber.d("Tag: $tag")
                }
                addClusterMarkers(newMarkers)

                viewModel.initPhotoPlaces(tags)
                initPlaceRecyclerView()
                clusterer.map = null
            }

            clusterer = newClusterer
            clusterer.map = naverMap
        }
    }

    private suspend fun makeMarker(
        markers: List<MarkerData>,
        builder: Clusterer.ComplexBuilder<MarkerData>,
        onClusterComplete: () -> Unit
    ): Clusterer<MarkerData> {
        val totalMarkers = markers.size
        var processedMarkers = 0

        val cluster: Clusterer<MarkerData> = builder.tagMergeStrategy { cluster ->
            cluster.children.map { it.tag }.joinToString(",")
        }
            .apply {
                clusterMarkerUpdater(object : DefaultClusterMarkerUpdater() {

                    override fun updateClusterMarker(info: ClusterMarkerInfo, marker: Marker) {

                        tags.add(info.tag.toString())
                        markerManager.releaseMarker(info, marker)

                        processedMarkers += info.size

                        if (processedMarkers == totalMarkers) {
                            onClusterComplete()
                        }
                    }
                })
            }
            .build()

        withContext(Dispatchers.Default) {
            markers.forEach { item ->
                cluster.add(item, "${item.id}")
            }
        }

        return cluster
    }

    private fun calculateRepresentativeCoordinate(cluster: List<MarkerData>): LatLng {
        val averageLatitude = cluster.map { it.photo.latitude }.average()
        val averageLongitude = cluster.map { it.photo.longitude }.average()
        return LatLng(averageLatitude, averageLongitude)
    }

    @SuppressLint("InflateParams")
    private fun addClusterMarkers(clusterGroups: List<List<MarkerData>>) {
        clusterGroups.forEach { cluster ->
            val representativeCoordinate = calculateRepresentativeCoordinate(cluster)

            val marker = Marker()
            marker.position = representativeCoordinate
            val customView = LayoutInflater.from(requireContext()).inflate(
                R.layout.marker_cluster_icon, null
            )
            val imageView = customView.findViewById<ImageView>(R.id.image_cluster)
            val firstPhotoIndex = cluster.first().id - 1
            val firstPhotoUrl = viewModel.markers.value[firstPhotoIndex].photo.filePath
            Glide.with(requireContext())
                .asBitmap()
                .apply(RequestOptions.circleCropTransform())
                .circleCrop()
                .override(144, 144)
                .load(firstPhotoUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        imageView.setImageBitmap(resource)
                        marker.icon = OverlayImage.fromView(customView)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                })
            marker.map = naverMap
        }
    }

    private fun initRecyclerView() {
        binding.photoAdapter = PhotoAdapter(viewModel)
        binding.rvPhotoAlbumDetail.setHasFixedSize(true)
    }

    private fun initPlaceRecyclerView() {
        binding.photoPlaceAdapter = PhotoPlaceAdapter(viewModel)
        binding.rvPlaceAlbumDetail.setHasFixedSize(false)
    }

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.PhotoUpload -> {
                findNavController().navigateSafely(R.id.action_album_detail_to_photo_upload)
            }

            is AlbumUiEvent.SelectPlace -> {
                binding.rvPlaceAlbumDetail.requestLayout()
            }

            else -> {}
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}