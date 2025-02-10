package com.neungi.moyeo.views.aiplanning

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSelectLocalBinding
import com.neungi.moyeo.databinding.ItemRegionCategoryBinding
import com.neungi.moyeo.databinding.ItemSelectedLocalChipBinding
import com.neungi.moyeo.views.aiplanning.adapters.LocationItemAdapter
import com.neungi.moyeo.views.aiplanning.viewmodel.AiPlanningUiEvent
import com.neungi.moyeo.views.aiplanning.viewmodel.AIPlanningViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
@AndroidEntryPoint
class AiSelectLocalFragment : BaseFragment<FragmentAiSelectLocalBinding>(R.layout.fragment_ai_select_local) {


    private val viewModel: AIPlanningViewModel by activityViewModels()

    lateinit var locationItemAdapter: LocationItemAdapter




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.toolbarAiLocalSelect.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        setupToggleGroup()
        setAdapter()
        observeStates()

    }
    private fun setAdapter() {
        locationItemAdapter = LocationItemAdapter(viewModel,viewLifecycleOwner)
        binding.rvDetailSelectLocal.adapter = locationItemAdapter
    }



    private fun setupToggleGroup() {
        val regions = resources.getStringArray(R.array.local_big).toList()
        regions.forEach { categoryName ->
            val buttonBinding = ItemRegionCategoryBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.toggleButtongroupCategory,
                false
            ).apply {
                vm = viewModel
                this.categoryName = categoryName
                root.id = View.generateViewId()
                executePendingBindings()
            }


            binding.toggleButtongroupCategory.addView(buttonBinding.root)
        }
        binding.toggleButtongroupCategory.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                val selectedButton = group.findViewById<MaterialButton>(checkedId)
                viewModel.selectLocalTab(selectedButton.text.toString())
            }
        }
        binding.toggleButtongroupCategory.check(binding.toggleButtongroupCategory.getChildAt(0).id)


    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLocations.collect { locations ->
                updateSelectedLocationChips(locations)
            }
        }
        collectLatestFlow(viewModel.selectedLocalTab){ tab->
            val regionId = viewModel.getRegionCategoryId(tab)
            val localDetailList = resources.getStringArray(regionId)
            locationItemAdapter.submitList(localDetailList.toList())


        }
        collectEvent()
    }
    private fun updateSelectedLocationChips(locations: List<String>) {
        binding.chipgroupSelectedLocations.removeAllViews()
        if(locations.isEmpty())binding.chipgroupSelectedLocations.visibility = View.GONE
        else binding.chipgroupSelectedLocations.visibility = View.VISIBLE
        locations.forEach { location ->
            val chipBinding = ItemSelectedLocalChipBinding.inflate(
                layoutInflater,
                binding.chipgroupSelectedLocations,
                false
            )
            chipBinding.text = location

            val chip = chipBinding.root

            chip.setOnClickListener {
                Timber.d(location)
                viewModel.toggleLocationSelection(location)
            }

            binding.chipgroupSelectedLocations.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        viewModel.selectLocalTab("특별")
    }

private fun collectEvent() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewModel.aiPlanningUiEvent.collect { event ->
            when (event) {
                is AiPlanningUiEvent.GoToDestination-> {
                    Timber.d("next")
                    findNavController().navigateSafely(R.id.action_ai_select_local_to_ai_destinaation)
                }
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