package com.neungi.moyeo.views.plan

import android.graphics.Path.Op
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.domain.model.Place
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSearchPlaceBinding
import com.neungi.moyeo.views.aiplanning.adapters.SearchPlaceAdapter
import com.neungi.moyeo.views.plan.adapter.AddPlaceAdapter
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleAddFragment :BaseFragment<FragmentAiSearchPlaceBinding>(R.layout.fragment_ai_search_place) {
    private val viewModel: ScheduleViewModel by activityViewModels()
    private lateinit var adapter: AddPlaceAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }
    private fun setupRecyclerView() {
        val placeList = mutableListOf(
            Place(placeName = "서울역", address = "교통", 0.0, 0.0),
            Place(placeName = "강남역", address = "교통", 0.0, 0.0),
            Place(placeName = "경복궁", address = "관광지", 0.0, 0.0),
            Place(placeName = "명동", address = "쇼핑", 0.0, 0.0),
            Place(placeName = "서울숲", address = "공원", 0.0, 0.0)
        )
        adapter = AddPlaceAdapter{

        }
        adapter.submitList(placeList)
        binding.rvAiSearchResult.adapter = adapter
        binding.rvAiSearchResult.layoutManager = LinearLayoutManager(context)
    }
}