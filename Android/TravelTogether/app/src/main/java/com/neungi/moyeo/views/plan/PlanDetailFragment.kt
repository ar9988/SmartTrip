package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.map.NaverMap
import com.neungi.domain.model.ServerReceive
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.ScheduleHeader
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.Section
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.SectionedAdapter
import com.neungi.moyeo.views.plan.scheduleviewmodel.websocket.createItemTouchHelperCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail) {
    private val tripId: Int by lazy {
        arguments?.getInt("tripId") ?: -1 // 기본값으로 -1을 사용
    }
    private val viewModel: ScheduleViewModel by activityViewModels()
    private lateinit var sectionedAdapter: SectionedAdapter
    private lateinit var naverMap: NaverMap

    //    private lateinit var scheduleAdapter: ScheduleAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        Timber.d("Received tripId: $tripId")
        viewModel.webSocketManager.events.observe(viewLifecycleOwner) { event : ServerReceive ->
            println("Received event: $event")
            sectionedAdapter.updatePosition(event)
            // list에 해당 sId의 positionPath 값 업데이트, rebuild 한다
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        sectionedAdapter = SectionedAdapter(
            onItemClick = { scheduleId ->
                println("click $scheduleId")
            },
            onDeleteClick = { scheduleId ->
                println("Delete schedule with ID: $scheduleId")
            },
            onEditClick = { scheduleId ->
                println("Edit schedule with ID: $scheduleId")
            },
            onAddClick = {
                findNavController().navigateSafely(R.id.action_schedule_add)
            },
            mutableListOf(
                Section(
                    ScheduleHeader(1, "1일차", 0),
                    mutableListOf(
                        ScheduleData(1, "일정1", 1000, 0, "식당", "30분"),
                        ScheduleData(2, "일정2", 2000, 0, "식당", "30분")
                    )
                ),
                Section(
                    ScheduleHeader(2, "2일차", 3000),
                    mutableListOf(
                        ScheduleData(3, "일정3", 4000, 0, "식당", "30분"),
                        ScheduleData(4, "일정4", 5000, 0, "식당", "30분"),
                        ScheduleData(5, "일정5", 6000, 0, "식당", "30분")
                    )
                )
            )
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = sectionedAdapter
        binding.recyclerView.setItemAnimator(null)
        // ItemTouchHelper 초기화
        val itemTouchHelperCallback = createItemTouchHelperCallback{ fromPosition, toPosition ->
            // 이동 이벤트를 ViewModel로 전달
            viewModel.onItemMoved(fromPosition, toPosition)
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

}