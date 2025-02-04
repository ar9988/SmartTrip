package com.neungi.moyeo.views.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentHomeBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.home.viewmodel.HomeUiEvent
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        collectLatestFlow(viewModel.homeUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(true)
    }

    private fun handleUiEvent(event: HomeUiEvent) {

    }
}