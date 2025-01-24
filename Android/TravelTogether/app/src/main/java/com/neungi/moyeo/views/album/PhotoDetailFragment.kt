package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoDetailBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhotoDetailFragment : BaseFragment<FragmentPhotoDetailBinding>(R.layout.fragment_photo_detail) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    private fun handleUiEvent(event: AlbumUiEvent) {

    }
}