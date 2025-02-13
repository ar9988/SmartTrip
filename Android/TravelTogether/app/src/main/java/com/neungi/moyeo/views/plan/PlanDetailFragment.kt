package com.neungi.moyeo.views.plan

import Member
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.MultipartPathOverlay
import com.naver.maps.map.overlay.Overlay
import com.neungi.data.entity.ManipulationEvent
import com.neungi.data.entity.PathReceive
import com.neungi.data.entity.ScheduleEntity
import com.neungi.data.entity.ServerReceive
import com.neungi.domain.model.ScheduleData
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.util.NonScrollableHorizontalLayoutManager
import com.neungi.moyeo.util.Section
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.adapter.PersonIconAdapter
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

    private var tripId : Int =-1

    // Map related properties
    private lateinit var naverMap: NaverMap
    private val paths = mutableMapOf<Int, List<LatLng>>()
    private val multipartPathOverlay = MultipartPathOverlay()
    private val markerMap = HashMap<Int, Marker>()

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

    private fun initializeViewBinding() {
        lifecycleScope.launch {
            tripViewModel.selectedTrip.collectLatest { trip ->
                trip?.let {
                    scheduleViewModel.initTrip(trip)
                    tripId = trip.id
                    Timber.d("Trip: ${scheduleViewModel.selectedTrip.value}")
                }
            }
        }
        with(binding) {
            Timber.d("Trip: ${scheduleViewModel.selectedTrip.value}")
            vm = scheduleViewModel
            tripViewModel.selectedTrip.value?.let { scheduleViewModel.initTrip(it) }
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
                handleMemberList(members)
            }
            editEvent.observe(viewLifecycleOwner) {
                handleEditSchedule(it)
            }
        }
    }

    private fun handleMemberList(members: List<Member>) {
        binding.rvPersonIconPlanDetail.adapter = PersonIconAdapter(members.map { it.userId })
        binding.rvPersonIconPlanDetail.layoutManager = NonScrollableHorizontalLayoutManager(requireContext())
        binding.rvPersonIconPlanDetail.setHasFixedSize(true) // RecyclerView 크기 고정
        binding.rvPersonIconPlanDetail.overScrollMode = View.OVER_SCROLL_NEVER // 오버스크롤 방지
        binding.rvPersonIconPlanDetail.isNestedScrollingEnabled = false // 내부 스크롤 비활성화
        binding.rvPersonIconPlanDetail.clipChildren = false
        binding.rvPersonIconPlanDetail.clipToPadding = false
        binding.rvPersonIconPlanDetail.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.rvPersonIconPlanDetail.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val recyclerViewWidth = binding.rvPersonIconPlanDetail.width // RecyclerView 가용 너비
                val itemWidth = dp2px(40f, binding.root.context) // 아이콘 크기
                val itemCount = binding.rvPersonIconPlanDetail.adapter?.itemCount ?: 1

                val maxSpacing = dp2px(8f, binding.root.context) // 충분한 공간이 있을 때 간격
                val minOverlap = -itemWidth * 0.9f // 겹치는 정도 (더 강하게 적용)

                // 전체 아이콘 너비 계산
                val totalItemWidth = (itemWidth * itemCount) + (maxSpacing * (itemCount - 1))

                // 공간이 충분할 경우 maxSpacing, 공간이 부족하면 겹치도록 조정
                val overlapOffset = if (totalItemWidth > recyclerViewWidth) minOverlap.toInt() else maxSpacing.toInt()

                // 기존 ItemDecoration 제거 (중복 적용 방지)
                while (binding.rvPersonIconPlanDetail.itemDecorationCount > 0) {
                    binding.rvPersonIconPlanDetail.removeItemDecorationAt(0)
                }

                // 새로운 ItemDecoration 추가
                binding.rvPersonIconPlanDetail.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                        val position = parent.getChildAdapterPosition(view)
                        if (position > 0) {
                            outRect.left = overlapOffset // 가변 간격 조정
                        }
                    }
                })

                binding.rvPersonIconPlanDetail.invalidateItemDecorations() // UI 즉시 반영
            }
        })
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
            sectionedAdapter.updatePathInfo(pathData, isUserDragging)
            if (!isUserDragging) {
                paintPathToMap()
            }
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
        binding.btnBackPlanDetail.setOnClickListener {
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
            handleMarker = { scheduleData, flag ->  handleMarker(scheduleData,flag)},
            recyclerView = binding.rvPlanDetail
        )

        binding.rvPlanDetail.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionedAdapter
            setHasFixedSize(true)
            itemTouchHelper.attachToRecyclerView(this)
        }

//        binding.rvPersonIconPlanDetail.apply {
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        val callback = createItemTouchHelperCallback(
            updatePosition = { from, to ->
                run {
                    scheduleViewModel.sendMoveEvent(from, to)
//                    removePathOverlay()
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

    private fun handleMarker(scheduleData: ScheduleData,flag:Boolean) {
        if(!flag) {
            markerMap[scheduleData.scheduleId]?.map = null
            markerMap.remove(scheduleData.scheduleId)
        } else if(markerMap.containsKey(scheduleData.scheduleId)) {
            return
        } else {
            val marker = Marker().apply {
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

    fun dp2px(dp: Float, context: Context) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)

    companion object {

        fun newInstance(tripId: Int) = PlanDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("tripId", tripId)
            }
        }
    }
}