package com.neungi.moyeo.views.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.neungi.domain.model.ApiStatus
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.DialogFestivalInfoBinding
import com.neungi.moyeo.databinding.FragmentHomeBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.home.adapter.HomeFestivalAdapter
import com.neungi.moyeo.views.home.viewmodel.HomeUiEvent
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var homeFestivalAdapter: HomeFestivalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        mainViewModel.setLoadingState(true)
        setAdapter()
        collectLatestFlow(mainViewModel.userLoginInfo){ userInfo->
            binding.loginInfo = userInfo
            binding.ivProfile.load(userInfo?.userProfileImg?:"https://i.namu.wiki/i/d1A_wD4kuLHmOOFqJdVlOXVt1TWA9NfNt_HA0CS0Y_N0zayUAX8olMuv7odG2FiDLDQZIRBqbPQwBSArXfEJlQ.webp") {
                crossfade(true)
                transformations(CircleCropTransformation())
                error(R.drawable.ic_profile)
            }
        }
        observeStates()
    }

//    private fun on

    private fun setAdapter() {
        homeFestivalAdapter = HomeFestivalAdapter(viewModel)
        binding.rvFestival.adapter =  homeFestivalAdapter
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(true)
    }

    private fun handleUiEvent(event: HomeUiEvent) {
        when(event){
            is HomeUiEvent.ShowFestivalDialog->{
                showFestivalDialog()

            }
            is HomeUiEvent.GoToNotification->{
                findNavController().navigateSafely(R.id.action_home_to_Notification)
            }
        }

    }

    fun observeStates(){
        collectLatestFlow(viewModel.homeUiEvent) { handleUiEvent(it) }
        collectLatestFlow(viewModel.recommendFestivals){ festivals ->
            if(festivals.isNotEmpty()) {
                homeFestivalAdapter.submitList(festivals)
            }
        }
        lifecycleScope.launch {
            viewModel.festivalState.collectLatest { state ->
                mainViewModel.setLoadingState(state.status==ApiStatus.LOADING)
            }
        }
    }

    fun showFestivalDialog(){
        val dialogBinding = DialogFestivalInfoBinding.inflate(layoutInflater)

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
            ivFestivalDialogImage.load(viewModel.dialogFestival.value!!.imageUrl){
                placeholder(R.drawable.ic_placeholder)
            }
            btnFestivalDialogClose.setOnClickListener {
                dialog.dismiss()
            }

        }

        dialog.show()
    }
}