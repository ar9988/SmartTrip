package com.neungi.moyeo.views.aiplanning

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAiSelectLocalBinding
import com.neungi.moyeo.views.aiplanning.adapters.RegionPagerAdapter
import com.neungi.moyeo.views.aiplanning.viwmodel.AIPlanningViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class AiSelectLocalFragment : BaseFragment<FragmentAiSelectLocalBinding>(R.layout.fragment_ai_select_local) {


    private val viewModel: AIPlanningViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.toolbarAiLocalSelect.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

//        binding.rvLocalBig.adapter = LocalAdapter(resources.getStringArray(R.array.local_big).toList())
        setupViewPager()
        binding.tablayoutLocalBig.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                (tab?.customView as? Chip)?.isChecked = true

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                (tab?.customView as? Chip)?.isChecked = false
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        observeStates()

    }
    private fun setupViewPager() {
        val viewPagerAdapter = RegionPagerAdapter(requireContext(),viewModel,viewLifecycleOwner)

        binding.vpLocalDetail.adapter = viewPagerAdapter

        // TabLayout과 ViewPager 연결
        TabLayoutMediator(binding.tablayoutLocalBig, binding.vpLocalDetail) { tab, position ->
            // Custom Chip View 생성 및 설정
            val chip = layoutInflater.inflate(R.layout.item_local_big_chip, null) as Chip
            chip.text = resources.getStringArray(R.array.local_big)[position]
            tab.customView = chip
        }.attach()

        // 첫 번째 탭 선택 상태로 설정
        val firstTab = binding.tablayoutLocalBig.getTabAt(0)?.customView as? Chip
        firstTab?.isChecked = true
    }



//    private fun setupRecyclerViews() {
//        binding.rvLocalBig.adapter = headerAdapter
//        binding.contentRecyclerView.adapter = contentAdapter
//    }

    private fun observeStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedLocations.collect { locations ->
                updateSelectedLocationChips(locations)
            }
        }
    }
    private fun updateSelectedLocationChips(locations: List<String>) {
        Timber.tag("??")
        binding.chipgroupSelectedLocations.removeAllViews()
        if(locations.isEmpty())binding.chipgroupSelectedLocations.visibility = View.GONE
        else binding.chipgroupSelectedLocations.visibility = View.VISIBLE
        locations.forEach { location ->
            val chip = layoutInflater.inflate(
                R.layout.item_selected_local_chip,
                binding.chipgroupSelectedLocations,
                false
            ) as Chip

            chip.text = location
            chip.setOnClickListener {  // closeIcon 대신 일반 클릭 리스너 사용
                Timber.d(location)
                viewModel.toggleLocationSelection(location)
            }

            binding.chipgroupSelectedLocations.addView(chip)
        }
    }

//    private fun updateLayout(state: RegionUiState) {
//        binding.rvLocalBig.layoutManager = when(state.layoutType) {
//            LayoutType.GRID -> GridLayoutManager(context, 3)
//            else-> GridLayoutManager(context, 3)
//        }
////        binding.contentRecyclerView.visibility = when(state) {
////            is RegionUiState.Grid -> View.GONE
////            is RegionUiState.Horizontal -> View.VISIBLE
////        }
//    }
}