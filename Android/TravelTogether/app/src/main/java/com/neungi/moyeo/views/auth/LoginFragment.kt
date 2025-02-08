package com.neungi.moyeo.views.auth

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentLoginBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.auth.viewmodel.AuthUiEvent
import com.neungi.moyeo.views.auth.viewmodel.AuthViewModel
import com.neungi.moyeo.views.setting.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    private val viewModel: AuthViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        setEditTextFocus()

        collectLatestFlow(viewModel.authUiEvent) { handleUiEvent(it) }
    }

    override fun onResume() {
        super.onResume()

        mainViewModel.setBnvState(false)
    }

    private fun setEditTextFocus() {
        with(binding) {
            showKeyboard(etIdLogin)
            tilPwLogin.setEndIconOnClickListener {
                etPwLogin.requestFocus()
            }
            etIdLogin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_NEXT) {
                    true -> {
                        etPwLogin.requestFocus()
                        true
                    }

                    else -> false
                }
            }
            etPwLogin.setOnEditorActionListener { _, actionId, _ ->
                when (actionId == EditorInfo.IME_ACTION_DONE) {
                    true -> {
                        if (btnLogin.isEnabled) {
                            viewModel.onClickLogin()
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
            is AuthUiEvent.LoginSuccess -> {
                mainViewModel.login()
                settingViewModel.getUserInfo()
                showToastMessage("${event.name}${resources.getString(R.string.text_login_success)}")
                requireActivity().supportFragmentManager.popBackStack()
            }

            is AuthUiEvent.LoginFail -> {
                showToastMessage(resources.getString(R.string.message_login_fail))
            }

            is AuthUiEvent.GoToJoin -> {
                findNavController().navigateSafely(R.id.action_login_to_join)
            }

            else -> {}
        }
    }
}