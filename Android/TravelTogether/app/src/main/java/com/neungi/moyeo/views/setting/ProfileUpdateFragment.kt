package com.neungi.moyeo.views.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.fragment.app.activityViewModels
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentProfileUpdateBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.setting.viewmodel.SettingUiEvent
import com.neungi.moyeo.views.setting.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("IntentReset")
@AndroidEntryPoint
class ProfileUpdateFragment :
    BaseFragment<FragmentProfileUpdateBinding>(R.layout.fragment_profile_update) {

    private val viewModel: SettingViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val profileImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                viewModel.initProfile(uri)
            } ?: showToastMessage(resources.getString(R.string.message_select_picture))
        }
    private val requestMultiPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                showToastMessage(getString(R.string.message_select_picture_permission))
            }
        }
    private val storageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    viewModel.initProfile(uri)
                } ?: showToastMessage(resources.getString(R.string.message_select_picture))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        viewModel.initUpdateUserInfo()
        initView()
        checkAndRequestPermissions()

        collectLatestFlow(viewModel.settingUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
    }

    private fun initView() {
        with(binding) {
            ivBackProfileUpdate.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!permissions.all { requireContext().checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
            requestMultiPermissions.launch(permissions)
        }
    }

    private fun getProfileImage() {
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        chooserIntent.putExtra(Intent.EXTRA_INTENT, intent)
        chooserIntent.putExtra(
            Intent.EXTRA_TITLE,
            resources.getString(R.string.message_select_picture)
        )
        storageLauncher.launch(chooserIntent)
    }

    private fun handleUiEvent(event: SettingUiEvent) {
        when (event) {
            is SettingUiEvent.GoToUploadProfileImage -> {
                if (isPhotoPickerAvailable(requireContext())) {
                    profileImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    getProfileImage()
                }
            }

            is SettingUiEvent.UpdateProfileSuccess -> {
                showToastMessage(resources.getString(R.string.message_update_profile))
                requireActivity().supportFragmentManager.popBackStack()
            }

            is SettingUiEvent.UpdateProfileFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_update_profile))
            }

            else -> {}
        }
    }
}