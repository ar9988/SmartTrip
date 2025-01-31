package com.neungi.moyeo.views.auth

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentJoinBinding
import com.neungi.moyeo.views.auth.viewmodel.AuthUiEvent
import com.neungi.moyeo.views.auth.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JoinFragment : BaseFragment<FragmentJoinBinding>(R.layout.fragment_join) {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        setEditTextFocus()

        collectLatestFlow(viewModel.authUiEvent) { handleUiEvent(it) }
    }

    private fun setEditTextFocus() {
        with(binding) {
            showKeyboard(etEmailJoin)
            tilPasswordJoin.setEndIconOnClickListener {
                etPasswordJoin.requestFocus()
            }
            tilPasswordAgainJoin.setEndIconOnClickListener {
                etPasswordAgainJoin.requestFocus()
            }
            etEmailJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etNameJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etNameJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPhoneNumberJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPhoneNumberJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPasswordJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPasswordJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPasswordAgainJoin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPasswordAgainJoin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_DONE) {
                    true -> {
                        if (btnJoin.isEnabled) {
                            viewModel.onClickJoinFinish()
                        }
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun handleUiEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.JoinSuccess -> {
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AuthUiEvent.JoinFail -> {
                showToastMessage(resources.getString(R.string.message_join_fail))
            }

            else -> {}
        }
    }
}