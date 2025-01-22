package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlanFragment: BaseFragment<FragmentPlanBinding>(R.layout.fragment_plan) {

    private val viewModel: TripViewModel by viewModels()
//    private lateinit var tripAdapter: TripAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        setupRecyclerView()

        // trips 데이터를 구독하여 RecyclerView에 표시
//        lifecycleScope.launch {
//            viewModel.trips.collect { trips ->
//                tripAdapter = TripAdapter(
//                    trips, { trip ->
//                        // 여행 항목 클릭 시 처리
//                    },
//                    { trip ->
//                        // Edit
//                    },
//                    { trip ->
//                        // Delete
//                    },
//                )
//                binding.recyclerViewTrips.adapter = tripAdapter
//            }
//        }

        // 예시로 trips 로드
//        viewModel.loadTripData()
    }
    private fun setupRecyclerView() {
        binding.recyclerViewTrips.layoutManager = LinearLayoutManager(context)
    }

}