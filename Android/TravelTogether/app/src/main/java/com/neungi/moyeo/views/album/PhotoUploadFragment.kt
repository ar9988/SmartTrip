package com.neungi.moyeo.views.album

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.isPhotoPickerAvailable
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoUploadBinding
import com.neungi.moyeo.views.album.adapter.PhotoUploadAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

@SuppressLint("IntentReset")
@AndroidEntryPoint
class PhotoUploadFragment :
    BaseFragment<FragmentPhotoUploadBinding>(R.layout.fragment_photo_upload) {

    private val viewModel: AlbumViewModel by activityViewModels()

    private val profileImagePicker =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                uris.forEach { uri ->
                    val path = absolutelyPath(uri)
                    path?.let {
                        val file = File(path)
                        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("files", file.name, requestBody)
                        viewModel.addUploadPhoto(uri, fetchPhotoTakenAt(uri), body)
                    }
                }
            } else {
                showToastMessage(resources.getString(R.string.message_select_picture))
            }
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

    private val storageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val clipData = result.data?.clipData
            if (clipData != null) {
                // 다중 선택의 경우
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    handleSelectedUri(uri)
                }
            } else {
                // 단일 선택의 경우
                result.data?.data?.let { uri ->
                    handleSelectedUri(uri)
                }
            }
        }
    }

    private fun handleSelectedUri(documentUri: Uri) {
        try {
            // 먼저 EXIF와 body 데이터 구하기
            val (latitude, longitude) = getLatLngFromExif(documentUri)
            val dateTaken = fetchPhotoTakenAt(documentUri)
            val body = createMultipartBody(documentUri)

            // Document URI를 MediaStore URI로 변환
            val mediaStoreUri = getMediaStoreUri(documentUri)

            // 변환된 URI와 함께 다른 데이터들 전달
            viewModel.addUploadPhoto(mediaStoreUri, dateTaken, body, latitude, longitude)

        } catch (e: Exception) {
            showToastMessage("이미지를 처리하는 중 오류가 발생했습니다.")
        }
    }

    private fun getMediaStoreUri(documentUri: Uri): Uri {
        // content://com.android.externalstorage.documents/document/primary:DCIM/Camera/20190805_220837.jpg
        // 형식에서 실제 경로 추출
        val path = documentUri.path?.let {
            it.substringAfter("primary:") // "DCIM/Camera/20190805_220837.jpg" 추출
        } ?: return documentUri

        // 파일명 추출
        val fileName = path.substringAfterLast("/") // "20190805_220837.jpg"

        // MediaStore에서 해당 파일 검색
        var projection = arrayOf(MediaStore.Images.Media._ID)
        var selection = "${MediaStore.Images.Media.DISPLAY_NAME} = ?"
        var selectionArgs = arrayOf(fileName)

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }
        }

        val idString = documentUri.lastPathSegment?.split(":")?.last() ?: return documentUri

        projection = arrayOf(MediaStore.Images.Media._ID)
        selection = MediaStore.Images.Media._ID + "=?"
        selectionArgs = arrayOf(idString)

        requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                return ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            }
        }

        return documentUri
    }

    private fun getLatLngFromExif(uri: Uri): Pair<Double, Double> {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    return Pair(latLong[0].toDouble(), latLong[1].toDouble())
                }
            }
        } catch (e: Exception) {
            Timber.d("Error reading EXIF: ${e.message}")
        }
        return Pair(0.0, 0.0)
    }

    private fun createMultipartBody(uri: Uri): MultipartBody.Part {
        val displayName = getFileName(uri) ?: "image_${System.currentTimeMillis()}"

        // ContentResolver를 통해 스트림 접근
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val byteArray = inputStream?.readBytes()
        inputStream?.close()

        // RequestBody 생성
        val requestBody = byteArray?.let {
            RequestBody.create("image/*".toMediaTypeOrNull(), it)
        } ?: throw IllegalStateException("Failed to read image data")

        return MultipartBody.Part.createFormData("files", displayName, requestBody)
    }

    private fun getFileName(uri: Uri): String? {
        return runCatching {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        }.getOrNull()
    }

    private fun fetchPhotoTakenAt(uri: Uri): Long {
        return runCatching {
            requireContext().contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    if (dateIndex != -1) {
                        cursor.getLong(dateIndex)
                    } else {
                        System.currentTimeMillis()
                    }
                } else {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()
        }.getOrDefault(System.currentTimeMillis())
    }
    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            // 읽기 권한만 요청
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        storageLauncher.launch(intent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.toolbarPhotoUpload.setNavigationOnClickListener {
            viewModel.onClickBackToAlbumDetail()
        }

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
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES,android.Manifest.permission.ACCESS_MEDIA_LOCATION)
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


    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.BackToAlbumDetail -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AlbumUiEvent.GoToStorage -> {
                openDocumentPicker()
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