package com.neungi.moyeo.views.plan

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.neungi.data.entity.ServerReceive
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.util.Permissions
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.album.AlbumDetailFragment
import com.neungi.moyeo.views.album.AlbumDetailFragment.Companion
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail),
    OnMapReadyCallback {
    private val viewModel: ScheduleViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var sectionedAdapter: SectionedAdapter
    private lateinit var naverMap: NaverMap
    private var isUserDragging = false  // 드래그 상태 추적
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationSource: FusedLocationSource


    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(false)
        viewModel.startConnect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.trip = viewModel.trip
        viewModel.serverEvents.observe(viewLifecycleOwner) { event: ServerReceive ->
            if (!isUserDragging) {
                Timber.d("Received external event: $event")
                sectionedAdapter.updatePosition(event)
            } else {
                sectionedAdapter.setPosition(event)
                Timber.d("Ignoring external event during user drag")
            }
        }
        viewModel.scheduleSections.observe(viewLifecycleOwner) { sections ->
            Timber.d(sections.toString())
            sectionedAdapter.sections = sections.toMutableList()
            sectionedAdapter.buildListItems()
        }
        viewModel.pathEvent.observe(viewLifecycleOwner) { pathEvent ->
//            sectionedAdapter.updatePathInfo(pathEvent)
        }
        setupRecyclerView()
        initFusedLocationClient()
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val itemTouchHelperCallback = createItemTouchHelperCallback({ fromPosition, toPosition ->
            // 이동 이벤트를 ViewModel로 전달
            viewModel.sendMoveEvent(fromPosition, toPosition)
        },
            { value ->
                isUserDragging = value
                if (!value) {
                    sectionedAdapter.rebuildSections()
                }
            },
            { position: Int ->
                sectionedAdapter.uiUpdate(position)
            },
            { position: Int ->
                viewModel.sendDeleteEvent(position)
            }
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        sectionedAdapter = SectionedAdapter(
            itemTouchHelper,
            onEditClick = { scheduleId ->
                println("Edit schedule with ID: $scheduleId")
            },
            onAddClick = {
                findNavController().navigateSafely(R.id.action_schedule_add)
            },
            mutableListOf()
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionedAdapter
            setHasFixedSize(true)
//            itemAnimator = null // 애니메이션 비활성화
        }

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun onStop() {
        super.onStop()
        viewModel.closeWebSocket()
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

    private fun initFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!hasPermission()) {
            requestLocationPermission()
        } else {
            initNaverMap()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
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

    private fun initNaverMap() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.detail_map_fragment) as MapFragment?
                ?: MapFragment.newInstance().also {
                    childFragmentManager.beginTransaction().add(R.id.detail_map_fragment, it)
                        .commit()
                }
        mapFragment.getMapAsync(this)
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

                    // 경로 그리기
                    val path = PathOverlay()
                    path.coords = listOf(
                        LatLng(37.57152, 126.97714),  // 예시 경로
                        LatLng(37.56607, 126.98268),  // 예시 경로
                        LatLng(37.56445, 126.97707),  // 예시 경로
                        LatLng(37.55855, 126.97822)   // 예시 경로
                    )

                    // 경로를 지도에 추가
                    path.map = naverMap

                    // 경로의 마지막 위치로 카메라 이동
                    val lastPosition = path.coords.last() // 경로의 마지막 좌표
                    Timber.d(lastPosition.toString())
                    val cameraUpdate = CameraUpdate.scrollTo(lastPosition)
                    naverMap.moveCamera(cameraUpdate)
                }
            }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}