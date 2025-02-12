package com.neungi.moyeo.views.plan

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.data.entity.ScheduleEntity
import com.neungi.domain.model.Place
import com.neungi.moyeo.R
import com.neungi.moyeo.views.aiplanning.AiSearchPlaceFragment
import com.neungi.moyeo.views.plan.adapter.AddPlaceAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class ScheduleAddFragment : AiSearchPlaceFragment() {

    private lateinit var addPlaceAdapter: AddPlaceAdapter
    private val tripId: Int by lazy { arguments?.getInt("tripId") ?: -1 }
    private val dayId: Int by lazy { arguments?.getInt("dayId") ?: -1 }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d(tripId.toString()+" "+dayId.toString())
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
    override fun observeState(){
        lifecycleScope.launch {
            mainViewModel.placeSearchResult.collect { places ->
                addPlaceAdapter.submitList(places)
            }
        }
    }

    override fun setAdapter() {
        // AddPlaceAdapter에 콜백을 등록하여 사용
        addPlaceAdapter = AddPlaceAdapter(viewModel) { selectedPlace ->
            // 선택된 장소에 대한 처리
            handlePlaceSelection(selectedPlace)
        }

        // 커스텀 어댑터 설정
        binding.rvAiSearchResult.adapter = addPlaceAdapter
    }

    private fun handlePlaceSelection(place: Place) {
        val scheduleEntity = ScheduleEntity(
            id = -1,
            placeName = place.placeName,
            tripId = tripId,
            positionPath = -1,
            day = dayId,
            lat = place.lat!!,
            lng = place.lng!!,
            type = 1,
            duration = 0
        )
        Timber.d(place.toString())
        // 다이어로그 생성
        val dialogView = layoutInflater.inflate(R.layout.dialog_schedule, null)
        val titleTextView = dialogView.findViewById<EditText>(R.id.et_dialog_title)
        val durationEditText = dialogView.findViewById<EditText>(R.id.et_duration)
        val closeBtn = dialogView.findViewById<ImageButton>(R.id.button_dialog_edit_close)
        titleTextView.setText(place.placeName)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // 확인 버튼 클릭 시 동작
        closeBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.button_confirm).setOnClickListener {
            val duration = durationEditText.text.toString().toIntOrNull() ?: 0

            scheduleEntity.duration = duration
            setFragmentResult("add", bundleOf("schedule" to scheduleEntity))
            // 이전 화면으로 돌아가기
            findNavController().popBackStack()

            dialog.dismiss() // 다이어로그 닫기
        }

        // 다이어로그 띄우기
        dialog.show()
    }
}