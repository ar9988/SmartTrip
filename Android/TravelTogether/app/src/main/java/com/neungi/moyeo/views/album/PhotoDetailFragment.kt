package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.domain.model.ApiStatus
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoDetailBinding
import com.neungi.moyeo.views.album.adapter.PhotoCommentAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhotoDetailFragment :
    BaseFragment<FragmentPhotoDetailBinding>(R.layout.fragment_photo_detail) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initRecyclerView()
        initView()
        setEditTextFocus()

        lifecycleScope.launch {
            viewModel.commentSubmitState.collectLatest { state ->
                when (state.status) {
                    ApiStatus.LOADING -> {
                        showLoading(true)
                    }

                    ApiStatus.ERROR -> {
                        showLoading(false)
                        showToastMessage(resources.getString(R.string.message_fail_to_comment_submit))
                    }

                    else -> { showLoading(false) }
                }
            }
        }

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    private fun initRecyclerView() {
        binding.adapter = PhotoCommentAdapter(viewModel)
        binding.rvPhotoDetail.setHasFixedSize(false)
    }

    private fun initView() {
        with(binding.fragmentPhotoDetail) {
            setOnRefreshListener {
                viewModel.initComments()
                binding.fragmentPhotoDetail.isRefreshing = false
                showToastMessage(resources.getString(R.string.message_refresh_photo))
            }
            setColorSchemeColors(resources.getColor(R.color.colorPrimary, context.theme))

        }
        binding.ivRefreshPhotoDetail.setOnClickListener {
            viewModel.initComments()
            showToastMessage(resources.getString(R.string.message_refresh_photo))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.lottieLoading.visibility = View.VISIBLE
            binding.lottieLoading.isClickable = true
            binding.lottieLoading.isFocusable = true
            binding.loadingAnimation.playAnimation()
        } else {
            binding.lottieLoading.visibility = View.GONE
            binding.lottieLoading.isClickable = false
            binding.lottieLoading.isFocusable = false
            binding.loadingAnimation.cancelAnimation()
        }
    }

    private fun setEditTextFocus() {
        with(binding) {
            etCommentPhotoDetail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (btnCommentSubmitPhotoDetail.isEnabled) {
                        viewModel.onClickCommentSubmit()
                        etCommentPhotoDetail.clearFocus()
                        hideKeyboard(etCommentPhotoDetail)
                    }
                    true
                } else {
                    false
                }
            }
        }
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