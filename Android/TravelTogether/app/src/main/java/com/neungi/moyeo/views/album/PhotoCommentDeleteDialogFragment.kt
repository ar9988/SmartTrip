package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseDialogFragment
import com.neungi.moyeo.databinding.FragmentPhotoCommentDeleteDialogBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhotoCommentDeleteDialogFragment :
    BaseDialogFragment<FragmentPhotoCommentDeleteDialogBinding>(R.layout.fragment_photo_comment_delete_dialog) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        lifecycleScope.launch {
            viewModel.albumUiEvent.collectLatest { uiEvent ->
                when (uiEvent) {
                    is AlbumUiEvent.PhotoCommentDeleteFinish -> dismiss()

                    else -> {}
                }
            }
        }

        binding.ivPhotoCommentDeleteDialog.setOnClickListener { dismiss() }
    }
}