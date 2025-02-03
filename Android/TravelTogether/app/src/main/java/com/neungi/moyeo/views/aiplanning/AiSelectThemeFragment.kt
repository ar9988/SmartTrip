package com.neungi.moyeo.views.aiplanning

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.neungi.domain.model.ThemeItem
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSelectPeriodBinding
import com.neungi.moyeo.databinding.FragmentAiThemeBinding
import com.neungi.moyeo.views.aiplanning.adapters.SelectThemeAdapter
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import com.neungi.moyeo.views.aiplanning.viewmodel.ThemeSelectUiState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AiSelectThemeFragment: BaseFragment<FragmentAiThemeBinding>(R.layout.fragment_ai_theme) {

    private val viewModel: AIPlanningViewModel by activityViewModels()
    private lateinit var themeAdapter: SelectThemeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.toolbarSelectTheme.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        loadThemes()
        setAdapter()
        observeStates()
        collectEvent()

    }

    private fun loadThemes() {
        val themeNames = resources.getStringArray(R.array.theme_names)
        val themeImages = resources.obtainTypedArray(R.array.theme_images)

        val themes = themeNames.indices.map { index ->
            ThemeItem(
                name = themeNames[index],
                imgId = themeImages.getResourceId(index, 0)
            )
        }

        themeImages.recycle()
        viewModel.setThemes(themes)
    }

    private fun setAdapter() {
        themeAdapter = SelectThemeAdapter{themeName->
            viewModel.toggleThemeSelection(themeName)
        }
        binding.rvTheme.adapter = themeAdapter

    }
    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // combine을 사용하여 두 Flow를 하나로 합치기
                combine(
                    viewModel.themeList,
                    viewModel.selectedThemeList
                ) { themes, selectedThemes ->
                    themes.map { theme ->
                        ThemeSelectUiState(
                            themeItem = theme,
                            isSelected = selectedThemes.contains(theme.name)
                        )
                    }
                }.collect { themeUiStates ->
                    themeAdapter.submitList(themeUiStates)
                }
            }
        }
    }

    private fun collectEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.aiPlanningUiEvent.collect { event ->
                when (event) {
                    is AiPlanningUiEvent.LimitToast ->{
                        showToastMessage(resources.getString(R.string.select_limit_toast_planning))
                    }

                    else->{

                    }
                }
            }
        }
    }
}