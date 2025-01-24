package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.map.NaverMap
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleData
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        println("Received tripId: $tripId")

        setupRecyclerView()
        lifecycleScope.launch {
            viewModel.scheduleData.collect {
//                sectionedAdapter.submitList(it)
            }
        }
    }
    private fun setupRecyclerView() {
        sectionedAdapter = SectionedAdapter(
            onItemClick = { scheduleId ->
                println("click $scheduleId")
                val action = PlanFragmentDirections.actionPlanToPlanDetail(tripId)
                findNavController().navigateSafely(actionId = R.id.action_plan_to_planDetail, args = action.arguments)
            },
            onDeleteClick = { scheduleId ->
                // 삭제 처리 로직
                println("Delete schedule with ID: $scheduleId")
            },
            onEditClick = { scheduleId ->
                // 편집 처리 로직
                println("Edit schedule with ID: $scheduleId")
            },
            mutableListOf(
                Section("1일차", mutableListOf(ScheduleData(4,"일정5 ","17시","19시","식당","30분"),ScheduleData(4,"일정5 ","17시","19시","식당","30분"))),
                Section("2일차", mutableListOf(ScheduleData(4,"일정5 ","17시","19시","식당","30분"),ScheduleData(4,"일정5 ","17시","19시","식당","30분"),ScheduleData(4,"일정5 ","17시","19시","식당","30분"))
                ))
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = sectionedAdapter
    }
}