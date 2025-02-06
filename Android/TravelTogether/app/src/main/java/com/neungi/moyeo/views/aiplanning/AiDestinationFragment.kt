    package com.neungi.moyeo.views.aiplanning

    import android.app.Dialog
    import android.graphics.Color
    import android.graphics.drawable.ColorDrawable
    import android.os.Bundle
    import android.view.View
    import android.view.Window
    import android.view.WindowManager
    import androidx.fragment.app.activityViewModels
    import androidx.lifecycle.Lifecycle
    import androidx.lifecycle.lifecycleScope
    import androidx.lifecycle.repeatOnLifecycle
    import androidx.navigation.fragment.findNavController
    import coil.load
    import com.neungi.moyeo.R
    import com.neungi.moyeo.config.BaseFragment
    import com.neungi.moyeo.databinding.DialogAddFestivalBinding
    import com.neungi.moyeo.databinding.DialogFestivalInfoBinding
    import com.neungi.moyeo.databinding.FragmentAiDestinationBinding
    import com.neungi.moyeo.views.aiplanning.adapters.AiRecommendFestivalAdapter
    import com.neungi.moyeo.views.aiplanning.adapters.SelectedLocationAdapter
    import com.neungi.moyeo.views.aiplanning.adapters.SelectedPlaceAdapter
    import com.neungi.moyeo.views.aiplanning.adapters.SelectedPlaceAdapter.PlaceUiModel
    import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
    import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
    import com.neungi.moyeo.views.aiplanning.viewmodel.FestivalSelectUiState
    import dagger.hilt.android.AndroidEntryPoint
    import kotlinx.coroutines.flow.collect
    import kotlinx.coroutines.flow.combine
    import kotlinx.coroutines.flow.conflate
    import kotlinx.coroutines.flow.distinctUntilChanged
    import kotlinx.coroutines.flow.filterNotNull
    import kotlinx.coroutines.flow.map
    import kotlinx.coroutines.launch

    @AndroidEntryPoint
    class AiDestinationFragment : BaseFragment<FragmentAiDestinationBinding>(R.layout.fragment_ai_destination) {

        private val viewModel: AIPlanningViewModel by activityViewModels()
        lateinit var festivalAdapter:AiRecommendFestivalAdapter

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            binding.toolbarAiDestination.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            binding.vm = viewModel
            setAdapter()
            collectEvent()
            observeState()
        }

        private fun observeState() {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.selectedLocations
                        .map { it.firstOrNull() }
                        .distinctUntilChanged()
                        .filterNotNull()
                        .collect { firstLocation ->
                            viewModel.updateFestivalsByLocation(firstLocation)
                        }

                }
            }


            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    combine(
                        viewModel.recommendFestivals,
                        viewModel.selectedPlaces
                    ) { festivals, selectedPlaces ->
                        festivals.map { festival ->
                            FestivalSelectUiState(
                                festival = festival,
                                isSelected = selectedPlaces.contains(festival.title)
                            )
                        }
                    }.collect { festivalUiStates ->
                        festivalAdapter.submitList(festivalUiStates)
                    }
                }
            }
        }

        private fun setAdapter() {
            val selectLocalAdapter = SelectedLocationAdapter(viewModel,viewLifecycleOwner)
            binding.rvLocal.adapter = selectLocalAdapter
            val selectSpotAdapter = SelectedPlaceAdapter(viewModel,viewLifecycleOwner)
            binding.rvPlace.adapter = selectSpotAdapter
            festivalAdapter = AiRecommendFestivalAdapter(viewModel)
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
                        is AiPlanningUiEvent.GoToTheme ->{
                            findNavController().navigateSafely(R.id.action_ai_destination_to_ai_select_theme)
                        }
                        is AiPlanningUiEvent.LimitToast ->{
                            showToastMessage(resources.getString(R.string.select_limit_toast_planning))
                        }
                        is AiPlanningUiEvent.ShowFestivalDialog ->{
                            showFestivalDialog()
                        }
                        else->{

                        }
                    }
                }
            }
        }

        fun showFestivalDialog(){
            val dialogBinding = DialogAddFestivalBinding.inflate(layoutInflater)

            // ViewModel 설정
            dialogBinding.vm = viewModel
            dialogBinding.lifecycleOwner = viewLifecycleOwner

            val dialog = Dialog(requireContext()).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(dialogBinding.root)

                // Dialog 크기 설정
                window?.apply {
                    val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
                    val height = (resources.displayMetrics.heightPixels * 0.7).toInt()
                    setLayout(
                        width,
                        height
                    )
                    setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
            }

            with(dialogBinding) {
                ivFestivalDialogImage.load(viewModel.dialogFestival.value!!.imageUrl)
                btnFestivalDialogClose.setOnClickListener {
                    dialog.dismiss()
                }

                btnFestivalDialogConfirm.setOnClickListener {
                    viewModel.togglePlaceSelection(viewModel.dialogFestival.value!!.title)
                    dialog.dismiss()
                }
            }

            dialog.show()
        }


    }