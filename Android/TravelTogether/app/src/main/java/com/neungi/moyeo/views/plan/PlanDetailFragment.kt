package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.naver.maps.map.NaverMap
import com.neungi.data.entity.ServerReceive
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPlanDetailBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlanDetailFragment : BaseFragment<FragmentPlanDetailBinding>(R.layout.fragment_plan_detail) {
    private val viewModel: ScheduleViewModel by activityViewModels()
    private val mainViewModel : MainViewModel by activityViewModels()
    private lateinit var sectionedAdapter: SectionedAdapter
    private lateinit var naverMap: NaverMap
    private var isUserDragging = false  // 드래그 상태 추적



    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
        viewModel.startConnect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.trip = viewModel.trip
        viewModel.serverEvents.observe(viewLifecycleOwner) { event: ServerReceive ->
            if (!isUserDragging) {
                Timber.d("Received external event: $event")
                sectionedAdapter.updatePosition(event)
            } else {
                sectionedAdapter.setPosition(event)
                Timber.d("Ignoring external event during user drag")
            }
        }
        viewModel.scheduleSections.observe(viewLifecycleOwner){ sections ->
            Timber.d(sections.toString())
            sectionedAdapter.sections = sections.toMutableList()
            sectionedAdapter.buildListItems()
        }
        setupRecyclerView()
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val itemTouchHelperCallback = createItemTouchHelperCallback({ fromPosition, toPosition ->
            // 이동 이벤트를 ViewModel로 전달
            viewModel.sendMoveEvent(fromPosition, toPosition)
        },
            { value ->
                isUserDragging = value
                if (!value) {
                    sectionedAdapter.rebuildSections()
                }
            },
            { position: Int ->
                sectionedAdapter.uiUpdate(position)
            },
            {
                position: Int ->
                viewModel.sendDeleteEvent(position)
            }
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        sectionedAdapter = SectionedAdapter(
            itemTouchHelper,
            onEditClick = { scheduleId ->
                println("Edit schedule with ID: $scheduleId")
            },
            onAddClick = {
                findNavController().navigateSafely(R.id.action_schedule_add)
            },
            mutableListOf()
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sectionedAdapter
            setHasFixedSize(true)
            itemAnimator = null // 애니메이션 비활성화
        }

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun onStop() {
        super.onStop()
        viewModel.closeWebSocket()
    }
}