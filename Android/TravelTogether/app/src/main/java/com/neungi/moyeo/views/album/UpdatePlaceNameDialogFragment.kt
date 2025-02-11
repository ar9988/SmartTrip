package com.neungi.moyeo.views.album

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseDialogFragment
import com.neungi.moyeo.databinding.FragmentUpdatePlaceNameDialogBinding
import com.neungi.moyeo.views.album.viewmodel.AlbumUiEvent
import com.neungi.moyeo.views.album.viewmodel.AlbumViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdatePlaceNameDialogFragment :
    BaseDialogFragment<FragmentUpdatePlaceNameDialogBinding>(R.layout.fragment_update_place_name_dialog) {

    private val viewModel: AlbumViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initView()
    }

    private fun initView() {
        with(binding) {
            ivUpdatePlaceNameDialog.setOnClickListener { dismiss() }
            etUpdatePlaceNameDialog.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (btnUpdatePlaceNameDialog.isEnabled) {
                        viewModel.onClickUpdatePlaceName()
                        etUpdatePlaceNameDialog.clearFocus()
                        hideKeyboard(etUpdatePlaceNameDialog)
                    }
                    true
                } else false
            }
        }
        lifecycleScope.launch {
            viewModel.albumUiEvent.collectLatest { uiEvent ->
                when (uiEvent) {
                    is AlbumUiEvent.UpdatePhotoPlaceNameSuccess -> dismiss()

                    else -> {}
                }
            }
        }
    }
}