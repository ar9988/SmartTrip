package com.neungi.moyeo.views.plan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.neungi.domain.model.LoginInfo
import com.neungi.domain.model.Trip
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.DialogAddTripBinding
import com.neungi.moyeo.databinding.FragmentPlanBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.plan.adapter.TripAdapter
import com.neungi.moyeo.views.plan.tripviewmodel.TripUiEvent
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class PlanFragment : BaseFragment<FragmentPlanBinding>(R.layout.fragment_plan) {

    private val tripViewModel: TripViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val aiPlaningViewModel: AIPlanningViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    private lateinit var user: LoginInfo
    private var flag = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = tripViewModel
        setupRecyclerView()

        binding.onAddClick = View.OnClickListener {
            showCalendarDialog()
        }

        lifecycleScope.launch {
            mainViewModel.userLoginInfo.collect {
                if (it != null) {
                    tripViewModel.getTrips(it.userId)
                    user = it
                }
            }
        }
        lifecycleScope.launch {
            tripViewModel.trips.collectLatest { tripList ->
                tripAdapter.submitList(tripList)
            }
        }

        lifecycleScope.launch {
            tripViewModel.trips.collectLatest {
                arguments?.let { argument ->
                    val tripId = argument.getInt("tripId", -1)
                    if (!flag && (tripId != -1)) {
                        tripViewModel.getTrip(tripId)
                    }
                }
            }
        }

        lifecycleScope.launch {
            tripViewModel.selectedTrip.collectLatest { trip ->
                if (trip != null) {
                    arguments?.let { argument ->
                        val tripId = argument.getInt("tripId", -1)
                        if (tripId != -1) {
                            tripViewModel.initTrip(trip)
                            Timber.d(trip.title)
                            findNavController().navigateSafely(R.id.action_plan_to_planDetail_pop_up_to)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            tripViewModel.tripUiEvent.collectLatest { event ->
                handleUiEvent(event)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(true)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onItemClick = { trip ->
                tripViewModel.initTrip(trip)
                Timber.d(trip.title)
                findNavController().navigateSafely(R.id.action_plan_to_planDetail)
            },
            onDeleteClick = { trip ->
                // 삭제 처리 로직
                showDeleteConfirmationDialog(trip)
            }
        )
        binding.recyclerViewTrips.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewTrips.adapter = tripAdapter
    }

    private fun showDeleteConfirmationDialog(trip: Trip) {
        // 다이어로그 생성
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("${trip.title} 을(를) 삭제하시겠습니까?")
            .setPositiveButton("확인") { dialogInterface, _ ->
                // "확인" 버튼을 눌렀을 때 삭제 작업 실행
                tripViewModel.deleteTrip(user.userId, trip.id)
                Toast.makeText(requireContext(), "${trip.title} 삭제되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                dialogInterface.dismiss()  // 다이어로그 닫기
            }
            .setNegativeButton("취소") { dialogInterface, _ ->
                // "취소" 버튼을 눌렀을 때 다이어로그만 닫기
                dialogInterface.dismiss()
            }
            .create()

        // 다이어로그 표시
        dialog.show()
    }

    private fun showCalendarDialog() {

        val dialogBinding = DialogAddTripBinding.inflate(LayoutInflater.from(context))
        dialogBinding.apply {
            vm = aiPlaningViewModel
        }

        val title = dialogBinding.editTextTitle
        val calendar = dialogBinding.calendarView
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("여행시작일과 종료일을 선택해주세요")
            .setView(dialogBinding.root)
            .setPositiveButton("추가하기") { dialogInterface, _ -> }
            .setNegativeButton("취소하기", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { v ->
            if (title.text.toString().isEmpty()) {
                Toast.makeText(requireContext(), "여행 제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else if (calendar.selectedStartDate == null || calendar.selectedEndDate == null) {
                Toast.makeText(requireContext(), "옳바른 날짜를 선택해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Timber.d(title.text.toString() + " " + calendar.selectedStartDate.toString() + " " + calendar.selectedEndDate)
                tripViewModel.createTrip(
                    user.userId,
                    title.text.toString(),
                    calendar.selectedStartDate!!,
                    calendar.selectedEndDate!!
                )
                dialog.dismiss()
            }
        }
    }

    private fun handleUiEvent(event: TripUiEvent) {
        when (event) {
            TripUiEvent.TripAdd -> {
                Toast.makeText(requireContext(), "여행이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                tripViewModel.getTrip(user.userId.toInt())
            }

            TripUiEvent.TripAddFail -> {
                Toast.makeText(requireContext(), "여행 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

            TripUiEvent.TripDelete -> {
                Toast.makeText(requireContext(), "여행이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

            }

            TripUiEvent.TripDeleteFail -> {
                Toast.makeText(requireContext(), "여행 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

            else -> {}

        }
    }
}