package com.neungi.moyeo.views.album

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neungi.domain.model.Photo
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentAlbumDetailWithPlaceBinding
import com.neungi.moyeo.views.album.adapter.PhotoAdapter
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AlbumDetailWithPlaceFragment :
    BaseFragment<FragmentAlbumDetailWithPlaceBinding>(R.layout.fragment_album_detail_with_place) {

    private val viewModel: AlbumViewModel by activityViewModels()
    private var recyclerViewState: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        binding.root.requestLayout()
    }

    private fun initRecyclerView() {
        arguments?.let { argument ->
            val placeTagIndex = argument.getInt("placeId")
            lifecycleScope.launch {
                with(binding.rvPhotoAlbumDetail) {
                    viewModel.photoPlaces.collectLatest { places ->
                        if (places.isNotEmpty()) {
                            recyclerViewState =
                                binding.rvPhotoAlbumDetail.layoutManager?.onSaveInstanceState()
                            val photos = when (places[placeTagIndex].name == "전체") {
                                true -> viewModel.photos.value

                                else -> photoFiltered(places[placeTagIndex].name.split(","))
                            }
                            Timber.d("${places[placeTagIndex].name} : $photos")
                            adapter = PhotoAdapter(photos, viewModel)
                            setHasFixedSize(true)
                            layoutManager?.onRestoreInstanceState(recyclerViewState)
                        }
                    }
                }
            }
        }
    }

    private fun photoFiltered(tags: List<String>): List<Photo> {
        val newPhotos = mutableListOf<Photo>()

        viewModel.photos.value.forEach { photo ->
            val id = photo.id
            if (tags.contains(id)) {
                newPhotos.add(photo)
            }
        }

        return newPhotos.toList()
    }

    companion object {

        fun newInstance(itemId: Long) = AlbumDetailWithPlaceFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", itemId.toInt())
            }
        }
    }
}