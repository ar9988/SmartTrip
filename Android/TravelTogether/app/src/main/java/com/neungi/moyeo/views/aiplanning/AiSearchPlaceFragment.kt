package com.neungi.moyeo.views.aiplanning

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSearchPlaceBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.aiplanning.adapters.SearchPlaceAdapter
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiSearchPlaceFragment : BaseFragment<FragmentAiSearchPlaceBinding>(R.layout.fragment_ai_search_place) {


    private val viewModel: AIPlanningViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var searchPlaceAdapter : SearchPlaceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = mainViewModel
        binding.toolbarSearchPlace.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.ivSearch.setOnClickListener {
            it.visibility = View.GONE
            binding.searchViewSearchSpot.visibility = View.VISIBLE
            binding.searchViewSearchSpot.requestFocus()
        }

        binding.searchViewSearchSpot.apply {
            setOnSearchClickListener {
                binding.ivSearch.visibility = View.GONE
            }
//            setOnCloseListener {
//                binding.ivSearch.visibility = View.VISIBLE
//                visibility = View.GONE
//                true
//            }
        }
        setAdapter()
        setListener()
        collectEvent()
        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            mainViewModel.placeSearchResult.collect { places ->
                searchPlaceAdapter.submitList(places)
            }
        }
    }

    private fun setListener() {

        binding.searchViewSearchSpot.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mainViewModel.onSearchTextChanged(newText)
                    return true
            }

        })
    }

    private fun setAdapter() {
        searchPlaceAdapter = SearchPlaceAdapter(viewModel)
        binding.rvAiSearchResult.adapter = searchPlaceAdapter
    }

    private fun collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aiPlanningUiEvent.collect { event ->
                when (event) {
                    is AiPlanningUiEvent.PopBackToDestination-> {
                        findNavController().popBackStack()
                    }
                    else->{

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainViewModel.clearSearchResult()
    }
}