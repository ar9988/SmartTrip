package com.neungi.moyeo.views.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.fragment.app.activityViewModels
import com.google.android.material.textfield.TextInputLayout
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentJoinBinding
import com.neungi.moyeo.views.auth.viewmodel.AuthUiEvent
import com.neungi.moyeo.views.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@SuppressLint("IntentReset")
@AndroidEntryPoint
class JoinFragment : BaseFragment<FragmentJoinBinding>(R.layout.fragment_join) {

    private val viewModel: AuthViewModel by activityViewModels()
    private val profileImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val path = absolutelyPath(uri)
                path?.let {
                    val file = File(path)
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("profile_image", file.name, requestBody)
                    viewModel.initProfile(uri, body)
                }
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
                    val path = absolutelyPath(uri)
                    val file = File(path.toString())
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("profile_image", file.name, requestBody)
                    viewModel.initProfile(uri, body)
                } ?: showToastMessage(resources.getString(R.string.message_select_picture))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        binding.tilPasswordJoin.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        setEditTextFocus()
        checkAndRequestPermissions()

        collectLatestFlow(viewModel.authUiEvent) { handleUiEvent(it) }
    }

    private fun setEditTextFocus() {
        with(binding) {
            showKeyboard(etEmailJoin)
            tilPasswordJoin.setEndIconOnClickListener {
                etPasswordJoin.requestFocus()
            }
            tilPasswordAgainJoin.setEndIconOnClickListener {
                etPasswordAgainJoin.requestFocus()
            }
            etEmailJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etNameJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etNameJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPhoneNumberJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPhoneNumberJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPasswordJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPasswordJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPasswordAgainJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPasswordAgainJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etProfileMessageJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etProfileMessageJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_DONE) {
                    true -> {
                        if (btnJoin.isEnabled) {
                            viewModel.onClickJoinFinish()
                        }
                        true
                    }

                    else -> false
                }
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

    private fun handleUiEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.GetProfileImage -> {
                if (isPhotoPickerAvailable(requireContext())) {
                    profileImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    getProfileImage()
                }
            }

            is AuthUiEvent.JoinSuccess -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AuthUiEvent.JoinFail -> {
                showToastMessage(resources.getString(R.string.message_join_fail))
            }

            else -> {}
        }
    }
}