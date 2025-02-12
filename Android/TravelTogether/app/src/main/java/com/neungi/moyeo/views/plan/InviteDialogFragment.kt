package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseDialogFragment
import com.neungi.moyeo.databinding.FragmentInviteDialogBinding
import com.neungi.moyeo.views.plan.scheduleviewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InviteDialogFragment :
    BaseDialogFragment<FragmentInviteDialogBinding>(R.layout.fragment_invite_dialog) {

    private val viewModel: ScheduleViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel

        initView()
    }

    private fun initView() {
        binding.ivInviteDialog.setOnClickListener { dismiss() }
    }
}