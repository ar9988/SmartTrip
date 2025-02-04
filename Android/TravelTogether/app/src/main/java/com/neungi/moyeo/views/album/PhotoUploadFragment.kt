package com.neungi.moyeo.views.album

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoUploadBinding
import com.neungi.moyeo.views.album.adapter.PhotoUploadAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@SuppressLint("IntentReset")
@AndroidEntryPoint
class PhotoUploadFragment :
    BaseFragment<FragmentPhotoUploadBinding>(R.layout.fragment_photo_upload) {

    private val viewModel: AlbumViewModel by activityViewModels()
    private val profileImagePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let {
                val path = absolutelyPath(uri)
                path?.let {
                    val file = File(path)
                    val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    viewModel.addUploadPhoto(uri, fetchPhotoTakenAt(uri), body)
                }
            } ?: showToastMessage(resources.getString(R.string.message_select_picture))
        }
    private val requestMultiPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                fetchPhotos()
            } else {
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
                    val body = MultipartBody.Part.createFormData("file", file.name, requestBody)
                    viewModel.addUploadPhoto(it, fetchPhotoTakenAt(it), body)
                } ?: showToastMessage(resources.getString(R.string.message_select_picture))
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initRecyclerView()
        checkAndRequestPermissions()

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    private fun initRecyclerView() {
        binding.adapter = PhotoUploadAdapter(viewModel)
        binding.rvPhotoUpload.setHasFixedSize(false)
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.all { requireContext().checkSelfPermission(it) == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
            fetchPhotos()
        } else {
            requestMultiPermissions.launch(permissions)
        }
    }

    private fun fetchPhotos() {
        viewModel.initUploadPhotos()
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

    private fun fetchPhotoTakenAt(uri: Uri?): Long {
        var result = 0L

        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN
        )

        uri?.let {
            requireContext().contentResolver.query(
                uri, projection, null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val takenIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    result = cursor.getLong(takenIndex)
                }
            }
        }

        return result
    }

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.BackToAlbumDetail -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AlbumUiEvent.GoToStorage -> {
                if (isPhotoPickerAvailable(requireContext())) {
                    profileImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    getProfileImage()
                }
            }

            is AlbumUiEvent.PhotoDuplicated -> {
                showToastMessage(resources.getString(R.string.message_photo_duplicated))
            }

            is AlbumUiEvent.GoToClassifyPlaces -> {
                findNavController().navigateSafely(R.id.action_photo_upload_to_photo_classification)
            }

            else -> {}
        }
    }
}