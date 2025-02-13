package com.neungi.moyeo.views.plan

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.share.WebSharerClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseDialogFragment
import com.neungi.moyeo.databinding.FragmentInviteDialogBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleUiEvent
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class InviteDialogFragment :
    BaseDialogFragment<FragmentInviteDialogBinding>(R.layout.fragment_invite_dialog) {

    private val viewModel: ScheduleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initView()
    }

    private fun initView() {
        binding.ivInviteDialog.setOnClickListener { dismiss() }
        binding.btnInviteDialog.setOnClickListener {
            viewModel.onClickInvite(arguments?.getInt("tripId") ?: -1)
        }
        lifecycleScope.launch {
            viewModel.scheduleUiEvent.collectLatest { uiEvent ->
                when (uiEvent) {
                    is ScheduleUiEvent.ScheduleInvite -> {
                        shareKakao(
                            uiEvent.userName,
                            arguments?.getString("tripTitle") ?: "",
                            uiEvent.token
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun shareKakao(userName: String, tripTitle: String, token: String) {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "모여(모두의 여행)",
                description = "${userName}님이 당신을 ${tripTitle}으로 초대하셨습니다.",
                imageUrl = "https://d210-traveltogether.s3.ap-northeast-2.amazonaws.com/default/image_app_main.png\n",
                link = Link(
                    androidExecutionParams = mapOf("token" to token),
                )
            ),
            buttons = listOf(
                Button(
                    "일정 수정하러가기",
                    Link(
                        androidExecutionParams = mapOf("token" to token),
                    )
                )
            )
        )

        Timber.d("Token: $token")

        if (ShareClient.instance.isKakaoTalkSharingAvailable(requireContext())) {
            // 카카오톡으로 카카오톡 공유 가능
            ShareClient.instance.shareDefault(
                requireContext(),
                defaultFeed
            ) { sharingResult, error ->
                if (error != null) {
                    Timber.e("카카오톡 공유 실패: $error")
                } else if (sharingResult != null) {
                    Timber.d("카카오톡 공유 성공", error)
                    startActivity(sharingResult.intent)

                    // 카카오톡 공유에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                    Timber.w("${sharingResult.warningMsg}")
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
            } catch (e: UnsupportedOperationException) {
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