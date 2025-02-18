package com.neungi.moyeo.views.plan

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.Overlay
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.Member
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.ScheduleData
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.util.ScheduleHeader
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import com.neungi.moyeo.views.plan.dialog.EditScheduleDialog
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail),
    OnMapReadyCallback {

    // ViewModels
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    private var tripId: Int = -1

    // Map related properties
    private lateinit var naverMap: NaverMap
    private val paths = mutableMapOf<Int, List<LatLng>>() // key = schedule ID, value = path
    private val multipartPathOverlay = MultipartPathOverlay()
    private val markerMap = HashMap<Int, Marker>()

    // Adapter
    private lateinit var sectionedAdapter: SectionedAdapter
    private var isUserDragging = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViewBinding()
        initializeMap()
//        setupRecyclerView()
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

    private fun initializeViewBinding() {
        lifecycleScope.launch {
            tripViewModel.selectedTrip.collectLatest { trip ->
                trip?.let {
                    scheduleViewModel.initTrip(trip)
                    tripId = trip.id
                }
            }
        }
        with(binding) {
            vm = scheduleViewModel
            tripViewModel.selectedTrip.value?.let { scheduleViewModel.initTrip(it) }
            tripViewModel.removeTrip()
        }
    }

    private fun setupObservers() {
        with(scheduleViewModel) {
            serverEvents.observe(viewLifecycleOwner) { event ->
                Timber.d(event.toString())
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
            memberList.observe(viewLifecycleOwner) { members ->
                Timber.d(members.toString())
                handleMemberList(members)
            }
            editEvent.observe(viewLifecycleOwner) {
                handleEditSchedule(it)
            }
        }
    }


    private fun handleServerEvent(event: ServerReceive) {
        sectionedAdapter.updateItem(event, isUserDragging)
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

            Timber.d(pathData.sourceScheduleId.toString())
            paths[pathData.sourceScheduleId] = convertToLatLngList(pathData.path)
        }
        sectionedAdapter.updatePathInfo(receive, isUserDragging)
        if (!isUserDragging) {
            paintPathToMap()
        }
    }

    private fun handleManipulationEvent(event: ScheduleData) {
        Timber.d("Add event ${event.toString()}")
        sectionedAdapter.addSchedule(event, isUserDragging)
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
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
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
    }

    private fun setupMultipartPathOverlay() {
        multipartPathOverlay.map = null
        val pathList = paths.values.toList()
        multipartPathOverlay.coordParts = pathList
        multipartPathOverlay.colorParts = createColorParts()
    }

    private fun createColorParts(): List<MultipartPathOverlay.ColorPart> {
        val list = mutableListOf<MultipartPathOverlay.ColorPart>()
        paths.keys.forEach {
            if(sectionedAdapter.pathInfo[it]!=null){
                list.add(
                    MultipartPathOverlay.ColorPart(
                        colors[sectionedAdapter.pathInfo[it]!!.first % colors.size],
                        Color.WHITE,
                        Color.DKGRAY,
                        Color.LTGRAY
                    )
                )
            }
        }
        return list
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
            handleMarker = { scheduleData, flag ->
                handleMarker(scheduleData, flag)
                moveCameraToShowAllMarker()
            },
            recyclerView = binding.rvPlanDetail
        )
        binding.rvPlanDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionedAdapter
            setHasFixedSize(true)
            itemTouchHelper.attachToRecyclerView(this)
            itemAnimator = null
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = createItemTouchHelperCallback(
            updatePosition = { from, to ->
                run {
                    scheduleViewModel.sendMoveEvent(from, to)
                }
            },
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
                handleScheduleDelete(scheduleId)
            }
        ).show()
    }

    private fun handleScheduleDelete(scheduleId: Int) {
        scheduleViewModel.sendDeleteEvent(scheduleId)
        sectionedAdapter.delete(scheduleId)
    }

    private fun handleScheduleEdit(scheduleData: ScheduleData) {
        sectionedAdapter.editItem(scheduleData, isUserDragging)
        val scheduleEntity = createScheduleEntity(scheduleData)
        scheduleEntity.tripId = tripId
        markerMap[scheduleData.scheduleId]?.apply {
            captionText = scheduleData.placeName
        }
        scheduleViewModel.sendEditEvent(scheduleEntity)
    }

    private fun createScheduleEntity(data: ScheduleData): ScheduleEntity {
        return ScheduleEntity(
            id = data.scheduleId,
            placeName = data.placeName,
            tripId = tripViewModel.selectedTrip.value?.id ?: -1,
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
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment_plan_detail) as? MapFragment
                ?: MapFragment.newInstance().also {
                    childFragmentManager.beginTransaction()
                        .add(R.id.map_fragment_plan_detail, it)
                        .commit()
                }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        this.naverMap = map.apply {
            uiSettings.isZoomControlEnabled = false
        }
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        val fineLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                fineLocation
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                coarseLocation
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    private fun moveCameraToShowAllMarker() {
        val bounds = calculateMarkersBounds()
        Timber.d(bounds.toString())
        val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun calculateMarkersBounds(): LatLngBounds {
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLng = Double.MAX_VALUE
        var maxLng = Double.MIN_VALUE

        // 모든 마커의 위치를 확인하여 경계를 계산합니다.
        markerMap.values.forEach { marker ->
            val position = marker.position
            minLat = minOf(minLat, position.latitude)
            maxLat = maxOf(maxLat, position.latitude)
            minLng = minOf(minLng, position.longitude)
            maxLng = maxOf(maxLng, position.longitude)
        }

        return LatLngBounds(
            LatLng(minLat, minLng),
            LatLng(maxLat, maxLng)
        )
    }

    private fun handleUiEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.GoToScheduleInvite -> {
                val bundle = Bundle().apply {
                    putInt("tripId", scheduleViewModel.selectedTrip.value?.id ?: -1)
                    putString("tripTitle", scheduleViewModel.selectedTrip.value?.title)
                }
                findNavController().navigate(R.id.action_plan_detail_to_invite, bundle)
            }

            else -> {}
        }
    }

    private fun handleMarker(scheduleData: ScheduleData, flag: Boolean) {
        if (!flag) {
            markerMap[scheduleData.scheduleId]?.map = null
            markerMap.remove(scheduleData.scheduleId)
        } else {
            val info = sectionedAdapter.pathInfo[scheduleData.scheduleId]
            if (scheduleData.lat < 0) return
            val marker = Marker().apply {
                iconTintColor = colors[info!!.first % colors.size]
                subCaptionText = info.first.toString() + " 일차 " + info.second.toString() + "번째 일정"
                width = 100
                height = 100
                position = LatLng(scheduleData.lat, scheduleData.lng)
                map = naverMap
                captionText = scheduleData.placeName
                isHideCollidedSymbols = true
                onClickListener = Overlay.OnClickListener { _ ->
                    showEditDialog(scheduleData)
                    true
                }
            }
            markerMap[scheduleData.scheduleId] = marker
        }
    }

    private fun handleMemberList(members: List<Member>) {
        val iconWidth = 40.dpToPx()
        var overlapMargin = 0
        if (iconWidth * members.size < binding.iconContainer.width) {
            overlapMargin = 0
        } else {
            overlapMargin =
                ((members.size * iconWidth - binding.iconContainer.width) / (members.size))
        }

        for (i in members.indices) {
            val imageView = ImageView(requireContext())
            Timber.d("URL " + members[i].profileImage)
            // Glide로 이미지를 로드
            Glide.with(requireContext())
                .load(members[i].profileImage)  // 각 Member의 이미지 URL
                .placeholder(R.drawable.baseline_account_circle_24)  // URL 로딩 전 기본 아이콘 표시
                .circleCrop()
                .into(imageView)

            // 아이콘의 레이아웃 파라미터 설정
            val params = FrameLayout.LayoutParams(iconWidth, iconWidth)
            params.gravity = Gravity.CENTER_VERTICAL
            // 각 아이콘의 leftMargin을 겹치도록 설정
            val margin = if (i > 0) ((iconWidth - overlapMargin) * i) else 0
            imageView.z = (members.size - i).toFloat()

            // 아이콘을 컨테이너에 추가
            binding.iconContainer.addView(imageView, params)
        }
    }


    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    companion object {
        val colors = listOf(
            Color.BLUE, Color.GREEN, Color.CYAN,
            Color.RED, Color.BLACK, Color.MAGENTA, Color.DKGRAY
        )

        fun newInstance(tripId: Int) = PlanDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("tripId", tripId)
            }
        }
    }
}