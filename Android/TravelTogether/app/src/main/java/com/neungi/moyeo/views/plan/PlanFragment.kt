package com.neungi.moyeo.views.plan

import TripAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanBinding
import com.neungi.moyeo.views.plan.tripviewmodel.TripUiEvent
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>(R.layout.fragment_plan) {

    private val viewModel: TripViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        binding.vm = viewModel
        lifecycleScope.launch {
            viewModel.trips.collect {
                tripAdapter.submitList(it)
            }
        }
    }

    private fun handleUiEvent(event: TripUiEvent) {
        when (event) {
            is TripUiEvent -> {

            }
        }
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onItemClick = { tripId ->
                println("click $tripId")
                val action = PlanFragmentDirections.actionPlanToPlanDetail(tripId)
                findNavController().navigateSafely(actionId = R.id.action_plan_to_planDetail, args = action.arguments)
            },
            onDeleteClick = { tripId ->
                // 삭제 처리 로직
                println("Delete trip with ID: $tripId")
            },
            onEditClick = { tripId ->
                // 편집 처리 로직
                println("Edit trip with ID: $tripId")
            }
        )
        binding.recyclerViewTrips.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewTrips.adapter = tripAdapter
    }

}