package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseDialogFragment
import com.neungi.moyeo.databinding.FragmentPhotoClassificationUpdateDialogBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhotoClassificationUpdateDialogFragment :
    BaseDialogFragment<FragmentPhotoClassificationUpdateDialogBinding>(R.layout.fragment_photo_classification_update_dialog) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        lifecycleScope.launch {
            viewModel.albumUiEvent.collectLatest { event ->
                when (event) {
                    is AlbumUiEvent.FinishPhotoClassificationUpdate -> dismiss()

                    else -> {}
                }
            }
        }

        initView()
    }

    private fun initView() {
        with(binding) {
            ivPhotoClassificationUpdateDialog.setOnClickListener { dismiss() }
            val places = viewModel.photoPlaces.value.filterNot { it.name == "전체" }
                .map { it.name }.toMutableList().apply { add("직접 입력") }.toList()
            spinnerPhotoClassificationUpdateDialog.adapter =
                ArrayAdapter(requireContext(), R.layout.item_spinner, places).apply {
                    setDropDownViewResource(R.layout.item_spinner_dropdown)
                }
            spinnerPhotoClassificationUpdateDialog.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        viewModel.selectedPlace(places[position])
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        viewModel.selectedPlace("")
                    }
                }
            etPhotoClassificationUpdateDialog.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (btnPhotoClassificationUpdateDialog.isEnabled) {
                        viewModel.onClickFinishUpdatePhotoClassification()
                        etPhotoClassificationUpdateDialog.clearFocus()
                        hideKeyboard(etPhotoClassificationUpdateDialog)
                    }
                    true
                } else false
            }
        }
    }
}