package com.neungi.moyeo.views.album

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentPhotoClassifiedBinding
import com.neungi.moyeo.views.album.adapter.PhotoClassifiedAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhotoClassifiedFragment :
    BaseFragment<FragmentPhotoClassifiedBinding>(R.layout.fragment_photo_classified) {

    private val viewModel: AlbumViewModel by activityViewModels()
    private var recyclerViewState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        arguments?.let { argument ->
            val index = argument.getInt("placeId")
            lifecycleScope.launch {
                with(binding.rvPhotoClassificationContent) {
                    viewModel.newMarkers.collectLatest { places ->
                        if (places.isNotEmpty()) {
                            recyclerViewState =
                                binding.rvPhotoClassificationContent.layoutManager?.onSaveInstanceState()
                            adapter = PhotoClassifiedAdapter(index, viewModel).apply {
                                submitList(viewModel.newMarkers.value[index].second)
                            }
                            setHasFixedSize(true)
                            layoutManager?.onRestoreInstanceState(recyclerViewState)
                        }
                    }
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(itemId: Long) = PhotoClassifiedFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", itemId.toInt())
            }
        }
    }
}