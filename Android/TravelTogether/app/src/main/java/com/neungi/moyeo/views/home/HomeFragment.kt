package com.neungi.moyeo.views.home

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
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
import timber.log.Timber

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
        binding.cardviewScheduleAddHome.setOnClickListener{
            shareKakao()
        }
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

    private fun shareKakao() {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "모여(모두의 여행)",
                description = "(초대자)님이 당신을 (일정이름)으로 초대하셨습니다.",
                imageUrl = "https://d210-traveltogether.s3.ap-northeast-2.amazonaws.com/default/image_app_main.png\n",
                link = Link(
                    androidExecutionParams = mapOf("key1" to "value1", "key2" to "value2"),
                )
            ),
            buttons = listOf(
                Button(
                    "일정 수정하러가기",
                    Link(
                        androidExecutionParams = mapOf("key1" to "value1", "key2" to "value2"),
                    )
                )
            )
        )

        if (ShareClient.instance.isKakaoTalkSharingAvailable(requireContext())) {
            // 카카오톡으로 카카오톡 공유 가능
            ShareClient.instance.shareDefault(requireContext(), defaultFeed) { sharingResult, error ->
                if (error != null) {
                    Timber.e("카카오톡 공유 실패: $error")
                }
                else if (sharingResult != null) {
                    Timber.d("카카오톡 공유 성공", error)
                    startActivity(sharingResult.intent)

                    // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Timber.w( "${sharingResult.warningMsg}")
                    Timber.w("Argument Msg: ${sharingResult.argumentMsg}")
                }
            }
        } else {
            // 카카오톡 미설치: 웹 공유 사용 권장
            // 웹 공유 예시 코드
            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)

            // CustomTabs으로 웹 브라우저 열기

            // 1. CustomTabsServiceConnection 지원 브라우저 열기
            // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
            try {
                KakaoCustomTabsClient.openWithDefault(requireContext(), sharerUrl)
            } catch(e: UnsupportedOperationException) {
                // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
            }

            // 2. CustomTabsServiceConnection 미지원 브라우저 열기
            // ex) 다음, 네이버 등
            try {
                KakaoCustomTabsClient.open(requireContext(), sharerUrl)
            } catch (e: ActivityNotFoundException) {
                // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
            }
        }
    }
}