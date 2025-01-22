package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import kotlinx.coroutines.launch

class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail) {

    private val viewModel: ScheduleViewModel by viewModels()
//    private lateinit var scheduleAdapter: ScheduleAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.vm = viewModel
//        setupRecyclerView()

        // trips 데이터를 구독하여 RecyclerView에 표시
//        lifecycleScope.launch {
//            viewModel.schedules.collect { schedules ->
//                scheduleAdapter = ScheduleAdapter(
//                    schedules, { schedule ->
//                        // 여행 항목 클릭 시 처리
//                    },
//                    { schedule ->
//                        // Edit
//                    },
//                    { schedule ->
//                        // Delete
//                    },
//                )
//                binding.recyclerViewSchedules.adapter = scheduleAdapter
//            }
//        }
//
//        // 예시로 trips 로드
//        viewModel.loadScheduleData()
//    }
//    private fun setupRecyclerView() {
//        binding.recyclerViewSchedules.layoutManager = LinearLayoutManager(context)
    }

}