package com.neungi.moyeo.views.plan

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.ScheduleData
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import com.neungi.moyeo.views.plan.dialog.EditScheduleDialog
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail),
    OnMapReadyCallback {

    // ViewModels
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    // Map related properties
    private lateinit var naverMap: NaverMap
    private val paths = mutableMapOf<Int, List<LatLng>>()
    private val multipartPathOverlay = MultipartPathOverlay()

    // Adapter
    private lateinit var sectionedAdapter: SectionedAdapter
    private var isUserDragging = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewBinding()
        initializeMap()
        setupObservers()
        setupRecyclerView()
        setupListeners()

        collectLatestFlow(scheduleViewModel.scheduleUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
        scheduleViewModel.startConnect()
    }

    override fun onStop() {
        super.onStop()

        scheduleViewModel.closeWebSocket()
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map.apply {
            uiSettings.isZoomControlEnabled = false
        }
        checkLocationPermission()
    }

    private fun initializeViewBinding() {
        with(binding) {
            vm = scheduleViewModel
            scheduleViewModel.initTrip(tripViewModel.trip)
        }
    }

    private fun setupObservers() {
        with(scheduleViewModel) {
            serverEvents.observe(viewLifecycleOwner) { event ->
                handleServerEvent(event)
            }
            scheduleSections.observe(viewLifecycleOwner) { sections ->
                handleScheduleSections(sections)
            }
            pathEvent.observe(viewLifecycleOwner) { receive ->
                handlePathEvent(receive)
            }
            manipulationEvent.observe(viewLifecycleOwner) { event ->
                handleManipulationEvent(event)
            }
        }
    }

    private fun handleServerEvent(event: ServerReceive) {
        sectionedAdapter.updatePosition(event, isUserDragging)
    }

    private fun handleScheduleSections(sections: List<Section>) {
        sectionedAdapter.apply {
            this.sections = sections.toMutableList()
            buildListItems()
            rebuildSections()
        }
    }

    private fun handlePathEvent(receive: PathReceive) {
        receive.paths.forEach { pathData ->
            paths[pathData.sourceScheduleId] = convertToLatLngList(pathData.path)
            sectionedAdapter.updatePathInfo(pathData, isUserDragging)
            if (!isUserDragging) {
                paintPathToMap()
            }
        }
    }

    private fun handleManipulationEvent(event: ManipulationEvent) {
        when (event.action) {
            "ADD" -> sectionedAdapter.addSchedule(event, isUserDragging)
            else -> handleEditSchedule(event)
        }
    }

    private fun handleEditSchedule(event: ManipulationEvent) {
        val scheduleData = createScheduleDataFromEvent(event)
        sectionedAdapter.editItem(scheduleData, isUserDragging)
    }

    private fun createScheduleDataFromEvent(event: ManipulationEvent): ScheduleData {
        return ScheduleData(
            scheduleId = event.schedule.id,
            placeName = event.schedule.placeName,
            positionPath = event.schedule.positionPath,
            timeStamp = event.timeStamp,
            type = event.schedule.type,
            lat = event.schedule.lat,
            lng = event.schedule.lng,
            duration = event.schedule.duration,
            fromTime = null,
            toTime = null
        )
    }

    private fun setupListeners() {
        binding.btnBackPlanDetail.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        setupFragmentResultListener()
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("add") { _, bundle ->
            val schedule = extractScheduleFromBundle(bundle)
            schedule?.let { showConfirmationDialog(it) }
        }
    }

    private fun extractScheduleFromBundle(bundle: Bundle): ScheduleEntity? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable("schedule", ScheduleEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable("schedule")
        }
    }

    private fun showConfirmationDialog(schedule: ScheduleEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("일정을 추가하시겠습니까?")
            .setMessage("스케줄 제목: ${schedule.placeName}")
            .setPositiveButton("확인") { _, _ ->
                scheduleViewModel.sendAddEvent(schedule)
            }
            .setNegativeButton("취소") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun paintPathToMap() {
        if (paths.isEmpty()) return

        setupMultipartPathOverlay()
        multipartPathOverlay.map = naverMap
        moveCameraToShowAllPaths()
    }

    private fun setupMultipartPathOverlay() {
        multipartPathOverlay.map = null
        val pathList = paths.values.toList()
        multipartPathOverlay.coordParts = pathList
        multipartPathOverlay.colorParts = createColorParts(pathList.size)
    }

    private fun createColorParts(pathCount: Int): List<MultipartPathOverlay.ColorPart> {
        val colors = listOf(
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
            Color.BLUE, Color.MAGENTA, Color.BLACK, Color.DKGRAY
        )
        return List(pathCount) { index ->
            MultipartPathOverlay.ColorPart(
                colors[index % colors.size],
                Color.WHITE,
                Color.DKGRAY,
                Color.LTGRAY
            )
        }
    }

    private fun convertToLatLngList(path: List<List<Double>>): List<LatLng> {
        return path.map { coords -> LatLng(coords[1], coords[0]) }
    }

    private fun setupRecyclerView() {
        val itemTouchHelper = createItemTouchHelper()

        sectionedAdapter = SectionedAdapter(
            itemTouchHelper = itemTouchHelper,
            onEditClick = { scheduleData -> showEditDialog(scheduleData) },
            onAddClick = { dayId -> navigateToAddSchedule(dayId) },
            onDeletePath = { scheduleId -> removePathOverlay(scheduleId) },
            recyclerView = binding.rvPlanDetail
        )

        binding.rvPlanDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionedAdapter
            setHasFixedSize(true)
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = createItemTouchHelperCallback(
            updatePosition = { from, to -> scheduleViewModel.sendMoveEvent(from, to) },
            onDrag = { isDragging ->
                isUserDragging = isDragging
                if (!isDragging) {
                    sectionedAdapter.rebuildSections()
                    paintPathToMap()
                }
            },
            uiUpdate = { position -> sectionedAdapter.uiUpdate(position) }
        )
        return ItemTouchHelper(callback)
    }

    private fun showEditDialog(scheduleData: ScheduleData) {
        EditScheduleDialog(
            context = requireContext(),
            scheduleData = scheduleData,
            onEdit = { editedData ->
                handleScheduleEdit(editedData)
            },
            onDelete = { scheduleId ->
                scheduleViewModel.sendDeleteEvent(scheduleId)
            }
        ).show()
    }

    private fun handleScheduleEdit(scheduleData: ScheduleData) {
        sectionedAdapter.editItem(scheduleData, isUserDragging)
        val scheduleEntity = createScheduleEntity(scheduleData)
        scheduleViewModel.sendEditEvent(scheduleEntity)
    }

    private fun createScheduleEntity(data: ScheduleData): ScheduleEntity {
        return ScheduleEntity(
            id = data.scheduleId,
            placeName = data.placeName,
            tripId = tripViewModel.trip.id,
            positionPath = data.positionPath,
            day = 0,
            lat = data.lat,
            lng = data.lng,
            type = data.type,
            duration = data.duration
        )
    }

    private fun navigateToAddSchedule(dayId: Int) {
        val bundle = Bundle().apply {
            putInt("tripId", scheduleViewModel.selectedTrip.value?.id ?: -1)
            putInt("dayId", dayId)
        }
        findNavController().navigate(R.id.action_schedule_add, bundle)
    }

    private fun removePathOverlay(scheduleId: Int) {
        paths.remove(scheduleId)
        paintPathToMap()
    }

    private fun initializeMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment_plan_detail) as? MapFragment
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction()
                    .add(R.id.map_fragment_plan_detail, it)
                    .commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun checkLocationPermission() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ActivityCompat.checkSelfPermission(requireContext(), fineLocation) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), coarseLocation) != PackageManager.PERMISSION_GRANTED) {
            return
        }
    }

    private fun moveCameraToShowAllPaths() {
        val bounds = calculatePathBounds()
        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun calculatePathBounds(): LatLngBounds {
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = Double.MIN_VALUE

        paths.values.forEach { path ->
            path.forEach { latLng ->
                minLat = minOf(minLat, latLng.latitude)
                maxLat = maxOf(maxLat, latLng.latitude)
                minLng = minOf(minLng, latLng.longitude)
                maxLng = maxOf(maxLng, latLng.longitude)
            }
        }

        return LatLngBounds(
            LatLng(minLat, minLng),
            LatLng(maxLat, maxLng)
        )
    }

    private fun handleUiEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.ScheduleInvite -> {
                findNavController().navigateSafely(R.id.action_plan_detail_to_invite)
            }

            else -> {}
        }
    }

    companion object {

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
    override fun onStop() {
        super.onStop()
//        scheduleViewModel.closeWebSocket()
    }
}