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
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.size
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.neungi.domain.model.Photo
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAlbumDetailBinding
import com.neungi.moyeo.util.CommonUtils.drawableToBitmap
import com.neungi.moyeo.util.MarkerData
import com.neungi.moyeo.util.Permissions
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.album.adapter.PhotoPlaceAdapter
import com.neungi.moyeo.views.album.adapter.PhotoPlaceAdapter.Companion.START_POSITION
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.abs

@AndroidEntryPoint
class AlbumDetailFragment :
    BaseFragment<FragmentAlbumDetailBinding>(R.layout.fragment_album_detail), OnMapReadyCallback {

    private val viewModel: AlbumViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initNaverMap()
            } else {
                showToastMessage(resources.getString(R.string.message_location_permission))
            }
        }
    private val clusteredMarkers = mutableListOf<Marker>()
    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initFusedLocationClient()
        initViews()

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
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
                    if (!this::naverMap.isInitialized) {
                        initNaverMap()
                    } else {
                        setMapToFitAllMarkers(viewModel.photos.value)
                        initClusterer()
                    }
                }
            }
    }

    private fun initFusedLocationClient() {
        binding.vpAlbumDetail.adapter = null
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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun setMapToFitAllMarkers(markers: List<Photo>) {
        if (markers.isEmpty()) return

        val boundsBuilder = LatLngBounds.Builder()
        markers.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = boundsBuilder.build()

        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun initClusterer() {
        lifecycleScope.launch {
            viewModel.markers.collectLatest { markers ->
                binding.vpAlbumDetail.adapter = PhotoPlaceAdapter(requireActivity(), 99)
                Timber.d("Size: ${binding.vpAlbumDetail.adapter?.itemCount}")
                clusteredMarkers.forEach { marker ->
                    marker.map = null
                }
                clusteredMarkers.clear()
                addClusterMarkers(markers)
                viewModel.initPhotoPlaces()
                initTabLayout(markers)
            }
        }
    }

    private fun calculateRepresentativeCoordinate(cluster: List<MarkerData>): LatLng {
        val averageLatitude = cluster.map { it.photo.latitude }.average()
        val averageLongitude = cluster.map { it.photo.longitude }.average()

        return LatLng(averageLatitude, averageLongitude)
    }

    @SuppressLint("InflateParams")
    private fun addClusterMarkers(clusterGroups: List<Pair<String, List<MarkerData>>>) {
        clusterGroups.forEachIndexed { index, cluster ->
            Timber.d("Index: $index")
            val representativeCoordinate = calculateRepresentativeCoordinate(cluster.second)

            val marker = Marker()
            marker.position = representativeCoordinate
            val customView = LayoutInflater.from(requireContext()).inflate(
                R.layout.marker_cluster_icon, null
            ).apply {
                when (index % 3) {
                    0 -> setBackgroundResource(R.drawable.ic_marker_red)

                    1 -> setBackgroundResource(R.drawable.ic_marker_blue)

                    2 -> setBackgroundResource(R.drawable.ic_marker_green)
                }
            }
            val imageView = customView.findViewById<ImageView>(R.id.image_cluster)
            val firstPhotoUrl = cluster.second.first().photo.filePath
            Glide.with(requireContext())
                .asBitmap()
                .apply(RequestOptions.circleCropTransform())
                .circleCrop()
                .override(192, 192)
                .placeholder(R.drawable.ic_theme_white)
                .error(R.drawable.ic_theme_white)
                .load(firstPhotoUrl)
                .into(object : CustomTarget<Bitmap>() {

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        imageView.setImageBitmap(resource)
                        marker.icon = OverlayImage.fromView(customView)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        imageView.setImageBitmap(errorDrawable?.let { drawableToBitmap(it) })
                        marker.icon = OverlayImage.fromView(customView)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                })
            marker.onClickListener = Overlay.OnClickListener {
                var placeIndex = 0
                viewModel.photoPlaces.value.forEachIndexed { index2, place ->
                    if (place.name == viewModel.photoPlaces.value[index + 1].name) {
                        viewModel.setPhotoPlace(index2)
                        placeIndex = index2
                    }
                }
                // setMapToFitAllMarkers(viewModel.markers.value[placeIndex - 1].second.map { it.photo })
                binding.vpAlbumDetail.setCurrentItem(placeIndex, true)
                true
            }
            marker.map = naverMap
            clusteredMarkers.add(marker)
        }
    }

    private fun initViews() {
        with(binding) {
            ablAlbumDetail.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                val isCollapsed = abs(verticalOffset) >= appBarLayout.totalScrollRange

                if (isCollapsed) {
                    toolbarAlbumDetail.visibility = View.GONE
                    toolbarAlbumDetailBottom.navigationIcon =
                        resources.getDrawable(R.drawable.baseline_chevron_left_24, context?.theme)
                } else {
                    toolbarAlbumDetail.visibility = View.VISIBLE
                    toolbarAlbumDetailBottom.navigationIcon = null
                }
            }
            toolbarAlbumDetail.bringToFront()
            toolbarAlbumDetail.setNavigationOnClickListener {
                Timber.d("터치")
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            toolbarAlbumDetailBottom.setNavigationOnClickListener {
                viewModel.onClickBackToAlbum()
            }
            ivRefreshAlbumDetail.setOnClickListener {
                viewModel.initPhotos()
            }
        }
    }

    private fun initTabLayout(markers: List<Pair<String, List<MarkerData>>>) {
        lifecycleScope.launch {
            viewModel.photoPlaces.collectLatest { places ->
                Timber.d("Markers: ${markers.size}, Places: ${places.size}")
                if (markers.size + 1 != places.size) return@collectLatest
                with(binding.vpAlbumDetail) {
                    binding.vpAlbumDetail.adapter = PhotoPlaceAdapter(requireActivity(), 99)
                    adapter = PhotoPlaceAdapter(requireActivity(), places.size)
                    setCurrentItem(START_POSITION, true)
                }
                TabLayoutMediator(binding.tlAlbumDetail, binding.vpAlbumDetail) { tab, position ->
                    tab.text = places[position].name
                }.attach()
                for (i in 0 until resources.getStringArray(R.array.local_big).size) {
                    val tabs = binding.tlAlbumDetail.getChildAt(0) as ViewGroup
                    for (tab in tabs.children) {
                        val lp = tab.layoutParams as LinearLayout.LayoutParams
                        lp.marginEnd = 16
                        tab.layoutParams = lp
                        binding.tlAlbumDetail.requestLayout()
                    }
                }
            }
        }
    }

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.BackToAlbum -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AlbumUiEvent.PhotoUpload -> {
                findNavController().navigateSafely(R.id.action_album_detail_to_photo_upload)
            }

            is AlbumUiEvent.SelectPhoto -> {
                binding.tlAlbumDetail.removeAllTabs()
                binding.vpAlbumDetail.adapter = null
                findNavController().navigateSafely(R.id.action_album_detail_to_photo_detail)
            }

            else -> {}
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}