package com.neungi.moyeo.views.plan

import android.os.Bundle
import android.view.View
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentScheduleAddBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleAddFragment :BaseFragment<FragmentScheduleAddBinding>(R.layout.fragment_schedule_add) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}