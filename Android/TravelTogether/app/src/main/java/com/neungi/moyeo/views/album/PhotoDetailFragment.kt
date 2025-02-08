package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoDetailBinding
import com.neungi.moyeo.views.album.adapter.PhotoCommentAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoDetailFragment :
    BaseFragment<FragmentPhotoDetailBinding>(R.layout.fragment_photo_detail) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initRecyclerView()

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    private fun initRecyclerView() {
        binding.adapter = PhotoCommentAdapter(viewModel)
        binding.rvPhotoDetail.setHasFixedSize(false)
    }

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.GetPhotoCommentsFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_get_photo_comments))
            }

            is AlbumUiEvent.DeletePhotoSuccess -> {
                showToastMessage(resources.getString(R.string.message_success_to_delete_photo))
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AlbumUiEvent.DeletePhotoFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_delete_photo))
            }

            is AlbumUiEvent.BackToAlbumDetail -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AlbumUiEvent.PhotoCommentSubmitSuccess -> {
                showToastMessage(resources.getString(R.string.message_comment_submit))
            }

            is AlbumUiEvent.PhotoCommentSubmitFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_comment_submit))
            }

            is AlbumUiEvent.PhotoCommentUpdateSuccess -> {
                showToastMessage(resources.getString(R.string.message_comment_update))
            }

            is AlbumUiEvent.PhotoCommentUpdateFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_comment_update))
            }

            is AlbumUiEvent.PhotoCommentDelete -> {
                findNavController().navigateSafely(R.id.action_photo_detail_to_photo_comment_delete)
            }

            is AlbumUiEvent.PhotoCommentDeleteFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_comment_delete))
            }

            else -> {}
        }
    }
}