package com.neungi.moyeo.views.album

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
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
import com.naver.maps.map.clustering.DefaultLeafMarkerUpdater
import com.naver.maps.map.clustering.LeafMarkerInfo
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.neungi.domain.model.ApiStatus
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoClassificationBinding
import com.neungi.moyeo.util.CommonUtils.initPlaceNumber
import com.neungi.moyeo.util.MarkerData
import com.neungi.moyeo.util.Permissions
import com.neungi.moyeo.views.album.adapter.PhotoClassificationAdapter
import com.neungi.moyeo.views.album.adapter.PhotoClassificationAdapter.Companion.START_POSITION
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class PhotoClassificationFragment :
    BaseFragment<FragmentPhotoClassificationBinding>(R.layout.fragment_photo_classification),
    OnMapReadyCallback {

    private val viewModel: AlbumViewModel by activityViewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initNaverMap()
            } else {
                showToastMessage(resources.getString(R.string.message_location_permission))
            }
        }
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource
    private lateinit var clusterer: Clusterer<MarkerData>
    private lateinit var markerBuilder: Clusterer.ComplexBuilder<MarkerData>
    private val tags = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.toolbarPhotoClassification.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        initFusedLocationClient()

        lifecycleScope.launch {
            viewModel.photoUploadState.collectLatest { state ->
                when (state.status) {
                    ApiStatus.LOADING -> {
                        showLoading(true)
                    }

                    ApiStatus.ERROR -> {
                        showLoading(false)
                        showToastMessage(resources.getString(R.string.message_fail_to_upload_photo))
                    }

                    else -> { showLoading(false) }
                }
            }
        }

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.maxZoom = 18.0

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
                    setMapToFitAllMarkers(viewModel.tempPhotos.value)
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
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun initNaverMap() {
        if (!hasPermission()) {
            requestLocationPermission()
            return
        }

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_photo_classification) as MapFragment?
                ?: MapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().add(R.id.map_photo_classification, it)
                        .commit()
                }
        mapFragment.getMapAsync(this)
    }

    private fun setMapToFitAllMarkers(markers: List<MarkerData>) {
        if (markers.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        markers.forEach { boundsBuilder.include(it.position) }
        val bounds = boundsBuilder.build()

        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun initClusterer() {
        markerBuilder = Clusterer.ComplexBuilder<MarkerData>()
        lifecycleScope.launch {
            viewModel.tempPhotos.collectLatest { markers ->
                tags.clear()
                val newClusterer = makeMarker(
                    markers,
                    markerBuilder
                ) {
                    val newMarkers = mutableListOf<List<MarkerData>>()
                    var photoIndex = initPlaceNumber(viewModel.photoPlaces.value.map { it.name })
                    Timber.d("Clusters Size: ${tags.size}")
                    tags.forEachIndexed { index, tag ->
                        val newLocations = mutableListOf<MarkerData>()
                        tag.split(",").forEach { id ->
                            newLocations.add(markers[id.toInt() - 1])
                        }

                        val placeName = newLocations.filterNot { it.photo.photoPlace == "" }
                            .groupBy { it.photo.photoPlace }.entries
                            .sortedWith(compareByDescending<Map.Entry<String, List<MarkerData>>> { it.value.size }
                                .thenBy { it.key }).firstOrNull()?.key ?: "장소 ${photoIndex++}"
                        tags[index] = placeName

                        calculateRepresentativeCoordinate(newLocations)
                        newMarkers.add(newLocations.filter { it.isNewPhoto })
                    }

                    viewModel.initNewMarkers(tags, newMarkers)
                    initTabLayout()
                    clusterer.map = null
                }

                clusterer = newClusterer
                clusterer.map = naverMap
            }
        }
    }

    private suspend fun makeMarker(
        markers: List<MarkerData>,
        builder: Clusterer.ComplexBuilder<MarkerData>,
        onClusterComplete: () -> Unit
    ): Clusterer<MarkerData> {
        val totalMarkers = markers.size
        Timber.d("Marker Size: $totalMarkers")
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
                }).leafMarkerUpdater(object : DefaultLeafMarkerUpdater() {

                    override fun updateLeafMarker(info: LeafMarkerInfo, marker: Marker) {
                        tags.add(info.tag.toString())

                        markerManager.releaseMarker(info, marker)

                        processedMarkers++

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

    private fun initTabLayout() {
        lifecycleScope.launch {
            viewModel.newMarkers.collectLatest { places ->
                with(binding.vpPhotoClassification) {
                    adapter = PhotoClassificationAdapter(
                        requireActivity(),
                        viewModel.newMarkers.value.size
                    )
                    setCurrentItem(START_POSITION, true)
                }
                TabLayoutMediator(
                    binding.tlPhotoClassification,
                    binding.vpPhotoClassification
                ) { tab, position ->
                    tab.text = places[position].first
                    tab.view.setOnLongClickListener {
                        viewModel.updatePlaceName(position)
                        true
                    }
                }.attach()
                for (i in 0 until resources.getStringArray(R.array.local_big).size) {
                    val tabs = binding.tlPhotoClassification.getChildAt(0) as ViewGroup
                    for (tab in tabs.children) {
                        val lp = tab.layoutParams as LinearLayout.LayoutParams
                        lp.marginEnd = 16
                        tab.layoutParams = lp
                        binding.tlPhotoClassification.requestLayout()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.lottieLoading.visibility = View.VISIBLE
            binding.lottieLoading.isClickable = true
            binding.lottieLoading.isFocusable = true
            binding.loadingAnimation.playAnimation()
        } else {
            binding.lottieLoading.visibility = View.GONE
            binding.lottieLoading.isClickable = false
            binding.lottieLoading.isFocusable = false
            binding.loadingAnimation.cancelAnimation()
        }
    }

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.UpdatePhotoClassification -> {
                findNavController().navigateSafely(R.id.action_photo_classification_to_photo_classification_update)
            }

            is AlbumUiEvent.UpdatePhotoPlaceName -> {
                findNavController().navigateSafely(R.id.action_photo_classification_to_update_place_name)
            }

            is AlbumUiEvent.FinishPhotoUpload -> {
                showToastMessage(resources.getString(R.string.message_photo_upload))
                findNavController().popBackStack(R.id.fragment_album_detail, false)
            }

            is AlbumUiEvent.PhotoUploadFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_upload_photo))
            }

            else -> {}
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}