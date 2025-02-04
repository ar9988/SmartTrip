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
                    viewModel.tempPlaces.collectLatest { places ->
                        if (places.isNotEmpty()) {
                            recyclerViewState =
                                binding.rvPhotoClassificationContent.layoutManager?.onSaveInstanceState()
                            adapter = PhotoClassifiedAdapter(index, viewModel).apply {
                                submitList(viewModel.tempPlaces.value[index].second)
                            }
                            setHasFixedSize(true)
                            layoutManager?.onRestoreInstanceState(recyclerViewState)
                        }
                    }
                }
            }

            initView(index)
        }
    }

    private fun initView(index: Int) {
        with(binding) {
            ivPhotoClassificationContent.setOnClickListener {
                ivPhotoClassificationContent.visibility = View.GONE
                ivEditPhotoClassificationContent.visibility = View.VISIBLE
                etPhotoClassificationContent.visibility = View.VISIBLE
                etPhotoClassificationContent.setText(viewModel.tempPlaces.value[index].first)
            }
            ivEditPhotoClassificationContent.setOnClickListener {
                when (viewModel.tempPlacesName.value.isBlank()) {
                    true -> {
                        showToastMessage("장소 이름을 입력해주세요.")
                    }

                    else -> {
                        viewModel.onClickUpdatePlaceName(index)
                        ivPhotoClassificationContent.visibility = View.VISIBLE
                        ivEditPhotoClassificationContent.visibility = View.GONE
                        etPhotoClassificationContent.visibility = View.GONE
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