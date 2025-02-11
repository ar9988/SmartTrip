package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.neungi.domain.model.ApiStatus
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAlbumBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.album.adapter.AlbumAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlbumFragment : BaseFragment<FragmentAlbumBinding>(R.layout.fragment_album) {

    private val viewModel: AlbumViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initRecyclerView()
        initView()

        lifecycleScope.launch {
            viewModel.albumsState.collectLatest { state ->
                when (state.status) {
                    ApiStatus.LOADING -> {
                        showLoading(true)
                    }

                    ApiStatus.ERROR -> {
                        showLoading(false)
                        showToastMessage(resources.getString(R.string.message_fail_to_get_albums))
                    }

                    else -> { showLoading(false) }
                }
            }
        }

        collectLatestFlow(viewModel.albumUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(true)
    }

    private fun initRecyclerView() {
        binding.adapter = AlbumAdapter(viewModel)
        binding.rvAlbum.setHasFixedSize(false)
    }

    private fun initView() {
        with(binding.fragmentAlbum) {
            setOnRefreshListener {
                viewModel.initAlbums()
                binding.fragmentAlbum.isRefreshing = false
            }
            setColorSchemeColors(resources.getColor(R.color.colorPrimary, context.theme))
        }
        binding.ivRefreshAlbum.setOnClickListener { viewModel.initAlbums() }
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

    private fun handleUiEvent(event: AlbumUiEvent) {
        when (event) {
            is AlbumUiEvent.GetAlbumsSuccess -> {
                showToastMessage(resources.getString(R.string.message_get_albums))
            }

            is AlbumUiEvent.GetAlbumsFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_get_albums))
            }

            is AlbumUiEvent.GoToAlbumDetail -> {
                findNavController().navigateSafely(R.id.action_album_to_album_Detail)
            }

            is AlbumUiEvent.GetAlbumDetailFail -> {
                showToastMessage(resources.getString(R.string.message_fail_to_get_album_detail))
            }

            else -> {}
        }
    }
}