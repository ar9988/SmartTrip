package com.neungi.moyeo.views.aiplanning

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiDestinationBinding
import com.neungi.moyeo.views.aiplanning.adapters.AiRecommendFestivalAdapter
import com.neungi.moyeo.views.aiplanning.adapters.SelectedLocationAdapter
import com.neungi.moyeo.views.aiplanning.adapters.SelectedPlaceAdapter
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiDestinationFragment : BaseFragment<FragmentAiDestinationBinding>(R.layout.fragment_ai_destination) {

    private val viewModel: AIPlanningViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbarAiDestination.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.vm = viewModel
        setAdapter()
        collectEvent()
    }

    private fun setAdapter() {
        val selectLocalAdapter = SelectedLocationAdapter(viewModel,viewLifecycleOwner)
        binding.rvLocal.adapter = selectLocalAdapter
        val selectSpotAdapter = SelectedPlaceAdapter(viewModel,viewLifecycleOwner)
        binding.rvPlace.adapter = selectSpotAdapter
        val festivalAdapter = AiRecommendFestivalAdapter(viewModel,viewLifecycleOwner)
        binding.rvFestival.adapter = festivalAdapter

    }
    private fun collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aiPlanningUiEvent.collect { event ->
                when (event) {
                    is AiPlanningUiEvent.GoToSelectLocal-> {
                        findNavController().navigateSafely(R.id.action_ai_destination_to_ai_select_local)
                    }
                    is AiPlanningUiEvent.GoToSearchPlace->{
                        findNavController().navigateSafely(R.id.action_ai_destination_to_ai_search_place)
                    }
                    else->{

                    }
                }
            }
        }
    }


}