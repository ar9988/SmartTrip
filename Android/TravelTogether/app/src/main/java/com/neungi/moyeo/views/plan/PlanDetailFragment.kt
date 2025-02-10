package com.neungi.moyeo.views.plan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerReceive
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail),
    OnMapReadyCallback {
    private val viewModel: ScheduleViewModel by viewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var sectionedAdapter: SectionedAdapter
    private lateinit var naverMap: NaverMap
    private var isUserDragging = false  // 드래그 상태 추적
    private val paths = mutableMapOf<Int, List<LatLng>>()
    private val multipartPathOverlay = MultipartPathOverlay()
    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(false)
        viewModel.startConnect()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.trip = tripViewModel.trip
        binding.trip = viewModel.trip
        initNaverMap()
        viewModel.serverEvents.observe(viewLifecycleOwner) { event: ServerReceive ->
            sectionedAdapter.updatePosition(event, isUserDragging)
        }
        viewModel.scheduleSections.observe(viewLifecycleOwner) { sections ->
            sectionedAdapter.sections = sections.toMutableList()
            sectionedAdapter.buildListItems()
            sectionedAdapter.rebuildSections()
        }
        viewModel.pathEvent.observe(viewLifecycleOwner) { receive ->
            receive.paths.forEach {
                paths[it.sourceScheduleId] = convertToLatLngList(it.path)
                sectionedAdapter.updatePathInfo(it, isUserDragging)
                if (!isUserDragging) {
                    paintPathToMap()
                }
            }
        }
        viewModel.addEvent.observe(viewLifecycleOwner) { event ->
            sectionedAdapter.addSchedule(event,isUserDragging)
        }
        setupRecyclerView()
        initNaverMap()
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        setFragmentResultListener("scheduleKey") { _, bundle ->
            val schedule = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("schedule", ScheduleEntity::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("schedule")
            }
            schedule?.let {
                Timber.d(schedule.toString())
                viewModel.sendAddEvent(it)
            }
        }
    }

    private fun paintPathToMap() {
        if (paths.isEmpty()) {
            return
        }
        multipartPathOverlay.map = null
        val pathList = paths.values.toList()
        multipartPathOverlay.coordParts = pathList
        val colorList = mutableListOf<MultipartPathOverlay.ColorPart>()
        val colors = listOf(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
            Color.BLUE, Color.MAGENTA, Color.BLACK, Color.DKGRAY
        )
        for (i in pathList.indices) {
            val colorIndex = i % colors.size  // 색상 순서 반복
            colorList.add(
                MultipartPathOverlay.ColorPart(
                    colors[colorIndex],
                    Color.WHITE,
                    Color.DKGRAY,
                    Color.LTGRAY
                )
            )
        }
        multipartPathOverlay.colorParts = colorList
        multipartPathOverlay.map = naverMap
        moveCameraToShowAllPaths()
    }

    private fun convertToLatLngList(path: List<List<Double>>): List<LatLng> {
        return path.map { coords ->
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
                    paintPathToMap()
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
            onAddClick = { dayId ->
                val bundle = Bundle().apply {
                    putInt("tripId", viewModel.trip.id)
                    putInt("dayId", dayId)
                }
                findNavController().navigate(
                    R.id.action_schedule_add,
                    bundle
                )
            },
            {
                scheduleId: Int ->
                removePathOverlay(scheduleId)
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

    private fun removePathOverlay(scheduleId:Int) {
        paths.remove(scheduleId)
        paintPathToMap()
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

    private fun moveCameraToShowAllPaths() {
        // pathList에서 모든 LatLng 좌표를 가져옵니다.
        val pathList = paths.values.toList()

        // 경로의 좌표들에서 최소/최대 위도와 경도를 계산합니다.
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = Double.MIN_VALUE

        pathList.forEach { path ->
            path.forEach { latLng ->
                // 각 경로에서 최소/최대 위도, 경도 값을 계산합니다.
                if (latLng.latitude < minLat) minLat = latLng.latitude
                if (latLng.latitude > maxLat) maxLat = latLng.latitude
                if (latLng.longitude < minLng) minLng = latLng.longitude
                if (latLng.longitude > maxLng) maxLng = latLng.longitude
            }
        }

        // 위도, 경도의 최소/최대 값으로 카메라의 범위를 설정합니다.
        val bounds = com.naver.maps.geometry.LatLngBounds(
            LatLng(minLat, minLng), // 남서쪽
            LatLng(maxLat, maxLng)  // 북동쪽
        )

        // 카메라를 계산된 범위로 이동시킵니다.
        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)  // 100은 패딩
        naverMap.moveCamera(cameraUpdate)
    }


    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}