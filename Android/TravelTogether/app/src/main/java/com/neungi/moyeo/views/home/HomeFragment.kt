package com.neungi.moyeo.views.home

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.neungi.domain.model.ApiStatus
import com.neungi.domain.model.Place
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.DialogFestivalInfoBinding
import com.neungi.moyeo.databinding.DialogPlaceInfoBinding
import com.neungi.moyeo.databinding.FragmentHomeBinding
import com.neungi.moyeo.util.CommonUtils
import com.neungi.moyeo.util.CommonUtils.formatZonedDateTimeWithZone
import com.neungi.moyeo.util.RegionMapper
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.home.adapter.HomeFestivalAdapter
import com.neungi.moyeo.views.home.adapter.HomePlaceAdapter
import com.neungi.moyeo.views.home.adapter.QuoteAdapter
import com.neungi.moyeo.views.home.viewmodel.HomeUiEvent
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel
import com.neungi.moyeo.views.home.viewmodel.Quote
import com.neungi.moyeo.views.plan.tripviewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val tripViewModel:TripViewModel by activityViewModels()
    lateinit var homeFestivalAdapter: HomeFestivalAdapter
    lateinit var homePlaceAdapter: HomePlaceAdapter
    lateinit var quoteAdapter: QuoteAdapter
    private var backPressedTime: Long = 0

    @Inject
    lateinit var regionMapper: RegionMapper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        viewModel.getLatestTrip()
        setAdapter()
        observeStates()
        loadQuotes()
    }

    override fun setBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - backPressedTime < 2000) {
                    requireActivity().finish()
                } else {
                    backPressedTime = System.currentTimeMillis()
                    showToastMessage("한 번 더 누르면 종료됩니다.")
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        homePlaceAdapter = HomePlaceAdapter(viewModel)
        binding.rvRecommendPlaceHome.adapter = homePlaceAdapter
        homeFestivalAdapter = HomeFestivalAdapter(viewModel)
        binding.rvFestival.adapter =  homeFestivalAdapter

    }

    override fun onResume() {
        super.onResume()
        Timber.d("HomeUiState festivals size: ${viewModel.homeUiState.value}")
        mainViewModel.setBnvState(true)
        binding.tvTitlePlan.isSelected = true
    }

    private fun handleUiEvent(event: HomeUiEvent) {
        when(event){
            is HomeUiEvent.ShowPlaceDialog->{
                showPlaceDialog()
            }
            is HomeUiEvent.ShowFestivalDialog->{
                showFestivalDialog()

            }
            is HomeUiEvent.GoToNotification->{
                findNavController().navigateSafely(R.id.action_home_to_Notification)
            }

            HomeUiEvent.GoToPlanDetail -> {
                viewModel.homeScheduleCardTrip.value?.let { trip ->
                    tripViewModel.initTrip(trip)
                    Timber.d(trip.title)
                    findNavController().navigateSafely(R.id.action_home_to_planDetail)
                }


            }
        }

    }

    fun observeStates(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.homeUiEvent.collect { event ->
                        handleUiEvent(event)
                    }
                }

                // UI 상태 수집
                launch {
                    viewModel.homeUiState.collect { state ->
                        binding.let { binding ->
                            mainViewModel.setLoadingState(state.isLoading)
                            binding.nscrollviewHome.visibility = if (state.isLoading == false) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        }
                        state.error?.let { error ->
                        }
                    }
                }

                // 새로고침 트리거 수집
                launch {
                    mainViewModel.refreshTrigger.collect { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.getRecommendPlace()
                            viewModel.getFestivalList()
                            mainViewModel.offRefreshTrigger()
                        }
                    }
                }
                launch {
                    mainViewModel.refreshPlanTrigger.collect { shouldRefresh ->
                        if (shouldRefresh) {
                            viewModel.getLatestTrip()
                            mainViewModel.offRefreshPlanTrigger()
                        }
                    }
                }
                launch{
                    mainViewModel.userLoginInfo.collect{ userInfo->
                        binding.let { binding ->
                            binding.loginInfo = userInfo
                            binding.ivProfile.load(
                                userInfo?.userProfileImg
                            ) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                                error(R.drawable.ic_profile)
                            }
                        }
                    }
                }

                launch {
                    viewModel.recommendFestivals.collect { festivals ->
                        if(festivals.isNotEmpty()) {
                            homeFestivalAdapter.submitList(festivals)
                            binding?.let{binding->
                                binding.tvFestival.visibility = View.VISIBLE
                                binding.rvFestival.visibility = View.VISIBLE
                            }
                        }else{
                            binding?.let{binding->
                                binding.tvFestival.visibility = View.GONE
                                binding.rvFestival.visibility = View.GONE
                            }

                        }
                    }
                }

                launch {
                    viewModel.recommendPlace.collect { places ->
                        if(places.isNotEmpty()) {
                            homePlaceAdapter.submitList(places)
                            binding?.let{binding->
                                binding.tvRecommendPlaceHome.visibility = View.VISIBLE
                                binding.rvRecommendPlaceHome.visibility = View.VISIBLE
                            }
                        }else{
                            binding?.let{binding->
                                binding.tvRecommendPlaceHome.visibility = View.GONE
                                binding.rvRecommendPlaceHome.visibility = View.GONE
                            }

                        }
                    }
                }

                launch{
                    viewModel.homeScheduleCardTrip.collect{ trip->
                        binding.let { binding ->
                            if(trip!=null){
                                val titles = trip.title.split(" ")
                                val image = regionMapper.getRegionDrawable(titles.firstOrNull() ?: "")
                                binding.tvDateRange.text = "${formatZonedDateTimeWithZone(trip.startDate)} ~ ${formatZonedDateTimeWithZone(trip.endDate)}"
                                binding.tvTodayLabel.text = CommonUtils.getDdayText(trip.startDate,trip.endDate)
                                binding.ivThumbnail.load(image){
                                    transformations(RoundedCornersTransformation(radius = 16f))
                                    error(R.drawable.image_noimg)
                                }
                            }
                        }
                    }
                }

            }
        }

    }

    private fun showFestivalDialog(){
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

    private fun showPlaceDialog(){
        val dialogBinding = DialogPlaceInfoBinding.inflate(layoutInflater)

        // ViewModel 설정
        dialogBinding.vm = viewModel
        dialogBinding.lifecycleOwner = viewLifecycleOwner

        val dialog = Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(dialogBinding.root)

            // Dialog 크기 설정
            window?.apply {
                val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
                val height = (resources.displayMetrics.heightPixels * 0.5).toInt()
                setLayout(
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }


        with(dialogBinding) {
            ivPlaceDialogImage.load(viewModel.dialogPlace.value!!.imageUrl){
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.image_noimg)
            }
            btnPlaceDialogClose.setOnClickListener {
                dialog.dismiss()
            }
            ivFollow.isSelected = viewModel.dialogPlace.value!!.isFollowed

            lifecycleScope.launch {
                viewModel.dialogPlace.collect { place ->
                    place?.let {
                        ivFollow.isSelected = it.isFollowed
                        Timber.d("Follow!!!")
                    }
                }
            }

            ivFollow.setOnClickListener{
                Timber.d("Follow!")
                viewModel.onClickFollow(viewModel.dialogPlace.value!!.contentId)
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

//    private fun shareKakao() {
//        val defaultFeed = FeedTemplate(
//            content = Content(
//                title = "모여(모두의 여행)",
//                description = "(초대자)님이 당신을 (일정이름)으로 초대하셨습니다.",
//                imageUrl = "https://d210-traveltogether.s3.ap-northeast-2.amazonaws.com/default/image_app_main.png",
//                link = Link(
//                    androidExecutionParams = mapOf("key1" to "value1", "key2" to "value2"),
//                )
//            ),
//            buttons = listOf(
//                Button(
//                    "일정 수정하러가기",
//                    Link(
//                        androidExecutionParams = mapOf("key1" to "value1", "key2" to "value2"),
//                    )
//                )
//            )
//        )
//
//        if (ShareClient.instance.isKakaoTalkSharingAvailable(requireContext())) {
//            // 카카오톡으로 카카오톡 공유 가능
//            ShareClient.instance.shareDefault(requireContext(), defaultFeed) { sharingResult, error ->
//                if (error != null) {
//                    Timber.e("카카오톡 공유 실패: $error")
//                }
//                else if (sharingResult != null) {
//                    Timber.d("카카오톡 공유 성공", error)
//                    startActivity(sharingResult.intent)
//
//                    // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
//                    Timber.w( "${sharingResult.warningMsg}")
//                    Timber.w("Argument Msg: ${sharingResult.argumentMsg}")
//                }
//            }
//        } else {
//            // 카카오톡 미설치: 웹 공유 사용 권장
//            // 웹 공유 예시 코드
//            val sharerUrl = WebSharerClient.instance.makeDefaultUrl(defaultFeed)
//
//            // CustomTabs으로 웹 브라우저 열기
//
//            // 1. CustomTabsServiceConnection 지원 브라우저 열기
//            // ex) Chrome, 삼성 인터넷, FireFox, 웨일 등
//            try {
//                KakaoCustomTabsClient.openWithDefault(requireContext(), sharerUrl)
//            } catch(e: UnsupportedOperationException) {
//                // CustomTabsServiceConnection 지원 브라우저가 없을 때 예외처리
//            }
//
//            // 2. CustomTabsServiceConnection 미지원 브라우저 열기
//            // ex) 다음, 네이버 등
//            try {
//                KakaoCustomTabsClient.open(requireContext(), sharerUrl)
//            } catch (e: ActivityNotFoundException) {
//                // 디바이스에 설치된 인터넷 브라우저가 없을 때 예외처리
//            }
//        }
//    }
}