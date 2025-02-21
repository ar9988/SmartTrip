package com.neungi.moyeo.views.setting

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentSettingBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.setting.viewmodel.SettingUiEvent
import com.neungi.moyeo.views.setting.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>(R.layout.fragment_setting) {

    private val viewModel: SettingViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initView()

        collectLatestFlow(viewModel.settingUiEvent) { handleUiEvent(it) }
        collectLatestFlow(mainViewModel.userLoginInfo) {
            viewModel.getUserInfo()
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getUserInfo()
        mainViewModel.setBnvState(true)
    }

    private fun initView() {
        with(binding) {
            ivProfileSetting.setOnClickListener {
                findNavController().navigateSafely(R.id.action_setting_to_profile_update)
            }
            tvNameSetting.setOnClickListener {
                findNavController().navigateSafely(R.id.action_setting_to_profile_update)
            }
            tvProfileMessageSetting.setOnClickListener {
                findNavController().navigateSafely(R.id.action_setting_to_profile_update)
            }
            ivTravelSetting.setOnClickListener {
                findNavController().navigate(
                    R.id.fragment_plan, null, NavOptions.Builder()
                        .setPopUpTo(R.id.fragment_setting, true)
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .build()
                )
            }
            tvTravelSetting.setOnClickListener {
                findNavController().navigate(
                    R.id.fragment_plan, null, NavOptions.Builder()
                        .setPopUpTo(R.id.fragment_setting, true)
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .build()
                )
            }
            ivAlbumSetting.setOnClickListener {
                findNavController().navigate(
                    R.id.fragment_album, null, NavOptions.Builder()
                        .setPopUpTo(R.id.fragment_setting, true)
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .build()
                )
            }
            tvAlbumSetting.setOnClickListener {
                findNavController().navigate(
                    R.id.fragment_album, null, NavOptions.Builder()
                        .setPopUpTo(R.id.fragment_setting, true)
                        .setLaunchSingleTop(true)
                        .setRestoreState(true)
                        .build()
                )
            }
        }
    }

    private fun handleUiEvent(event: SettingUiEvent) {
        when (event) {
            is SettingUiEvent.GoToLogin -> {
                findNavController().navigateSafely(R.id.action_setting_to_login)
            }

            is SettingUiEvent.GoToUpdateProfile -> {
                findNavController().navigateSafely(R.id.action_setting_to_profile_update)
            }

            is SettingUiEvent.Logout -> {
                mainViewModel.logout()
            }

            else -> {}
        }
    }
}