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
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
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
    private val pathOverlays = mutableMapOf<Int, PathOverlay>()

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
            Timber.d("start",sections.toString())
            sectionedAdapter.sections = sections.toMutableList()
            sectionedAdapter.buildListItems()
        }
        viewModel.pathEvent.observe(viewLifecycleOwner) { path ->
            if(!isUserDragging){
                sectionedAdapter.updatePathInfo(path)
                pathOverlays[path.sourceScheduleId]?.let { pathOverlay ->
                    removePathOverlay(pathOverlay)
                    pathOverlays.remove(path.sourceScheduleId)
                }
                val overlay = PathOverlay().apply {
                    coords = convertToLatLngList(path.path)
                    color = 0xff0000ff.toInt()
                    width = 10
                }
                overlay.map = naverMap
                pathOverlays[path.sourceScheduleId] = overlay
                pathOverlays[path.sourceScheduleId]?.map = naverMap
                adjustCameraToPath(overlay)
            }else{
                sectionedAdapter.setPathInfo(path)
            }
        }
        setupRecyclerView()
        initNaverMap()
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun removePathOverlay(path: PathOverlay){
        path.map = null
    }

    fun convertToLatLngList(path: List<List<Double>>): List<LatLng> {
        return path.map { coords ->
            // 각 List<Double>을 LatLng으로 변환 (coords[0] = latitude, coords[1] = longitude)
            LatLng(coords[1], coords[0])
        }
    }

    private fun setupRecyclerView() {
        val itemTouchHelperCallback = createItemTouchHelperCallback({ fromPosition, toPosition ->
            // 이동 이벤트를 ViewModel로 전달
            viewModel.sendMoveEvent(fromPosition, toPosition)
        },
            { value ->
                isUserDragging = value
                if (!isUserDragging) {
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
            binding.recyclerView
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionedAdapter
            setHasFixedSize(true)
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


    private fun initNaverMap() {
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
        this.naverMap.uiSettings.isZoomControlEnabled = false
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

    }

    private fun adjustCameraToPath(path: PathOverlay) {
        // 경로의 시작점과 끝점을 얻기
        val startLatLng = path.coords.first()  // 경로의 첫 번째 좌표
        val endLatLng = path.coords.last()    // 경로의 마지막 좌표

        // 중간 지점을 계산 (경로의 중앙 위치)
        val centerLat = (startLatLng.latitude + endLatLng.latitude) / 2
        val centerLng = (startLatLng.longitude + endLatLng.longitude) / 2
        val centerLatLng = LatLng(centerLat, centerLng)

        // 카메라 줌 레벨 계산 (경로의 범위를 기준으로 줌을 설정)
        var zoom = 16.0
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = Double.MIN_VALUE

        // 경로의 범위 계산 (최소 및 최대 경도/위도)
        path.coords.forEach {
            minLat = minOf(minLat, it.latitude)
            maxLat = maxOf(maxLat, it.latitude)
            minLng = minOf(minLng, it.longitude)
            maxLng = maxOf(maxLng, it.longitude)
        }

        // 경로가 지도에 맞게 보이도록 줌 레벨을 조정
        while (zoom >= 1.0) {
            val cameraPosition = CameraPosition(centerLatLng, zoom)
            naverMap.setCameraPosition(cameraPosition)

            // 지도에서 경로 범위 확인
            val bounds = naverMap.contentBounds
            if (startLatLng.latitude in bounds.southWest.latitude..bounds.northEast.latitude &&
                startLatLng.longitude in bounds.southWest.longitude..bounds.northEast.longitude &&
                endLatLng.latitude in bounds.southWest.latitude..bounds.northEast.latitude &&
                endLatLng.longitude in bounds.southWest.longitude..bounds.northEast.longitude) {
                return // 경로가 지도 범위에 들어오면 종료
            }

            zoom -= 0.5  // 줌 레벨을 0.5씩 감소시킴
        }
    }



    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}