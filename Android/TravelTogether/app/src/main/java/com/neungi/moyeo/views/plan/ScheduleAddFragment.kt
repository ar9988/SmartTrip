package com.neungi.moyeo.views.plan

import android.graphics.Path.Op
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.domain.model.AddRequest
import com.neungi.domain.model.Operation
import com.neungi.domain.model.Place
import com.neungi.domain.model.ServerEvent
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentScheduleAddBinding
import com.neungi.moyeo.views.plan.adapter.PlaceAdapter
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleAddFragment :BaseFragment<FragmentScheduleAddBinding>(R.layout.fragment_schedule_add) {
    private val viewModel: ScheduleViewModel by activityViewModels()
    private lateinit var adapter: PlaceAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }
    private fun setupRecyclerView() {
        val placeList = mutableListOf(
            Place(title = "서울역", category = "교통"),
            Place(title = "강남역", category = "교통"),
            Place(title = "경복궁", category = "관광지"),
            Place(title = "명동", category = "쇼핑"),
            Place(title = "서울숲", category = "공원")
        )
        adapter = PlaceAdapter { viewModel.webSocketManager.sendMessage(
            AddRequest("ADD",1,1,"Test",1)
        ) }
        adapter.submitList(placeList)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }
}