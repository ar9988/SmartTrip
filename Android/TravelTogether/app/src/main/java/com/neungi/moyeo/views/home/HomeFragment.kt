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
import com.neungi.moyeo.views.home.adapter.QuoteAdapter
import com.neungi.moyeo.views.home.viewmodel.HomeUiEvent
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import com.neungi.moyeo.views.home.viewmodel.Quote
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var homeFestivalAdapter: HomeFestivalAdapter
    lateinit var quoteAdapter: QuoteAdapter

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
        loadQuotes()
    }

//    private fun on

    private fun setAdapter() {
        quoteAdapter = QuoteAdapter()
        binding.vpQuoteHome.apply {
            adapter = quoteAdapter
            // 시작 위치를 중간으로 설정하여 양방향 무한 스크롤 가능하게
            setCurrentItem(Int.MAX_VALUE / 2, false)

            // 자동 스크롤
            lifecycleScope.launch {
                while(true) {
                    delay(5000)
                    setCurrentItem(currentItem + 1, true)
                }
            }
        }
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

    private fun loadQuotes() {

        val quoteStrings = resources.getStringArray(R.array.quote)

        val quotes = quoteStrings.map { quoteString ->
            // \n으로 분리하여 인용구와 저자 정보 분리
            val parts = quoteString.split("\n")
            val quoteText = parts[0].trim('"') // 따옴표 제거
            val authorInfo = parts[1]

            // 저자 정보에서 책 제목과 저자 분리
            val authorParts = if (authorInfo.contains("&lt;")) {
                val bookTitle = authorInfo.substringBetween("&lt;", "&gt;")
                val author = authorInfo.substringAfter("&gt;").trim(',', ' ', '-')
                Pair(bookTitle, author)
            } else {
                Pair(null, authorInfo)
            }

            Quote(
                text = quoteText,
                author = authorParts.second,
                source = authorParts.first,
                backgroundImageRes = R.drawable.image_camping
            )
        }

        // 어댑터에 데이터 설정
        quoteAdapter.setQuotes(quotes)
    }

    private fun String.substringBetween(start: String, end: String): String {
        val startIndex = this.indexOf(start) + start.length
        val endIndex = this.indexOf(end)
        return this.substring(startIndex, endIndex)
    }
}