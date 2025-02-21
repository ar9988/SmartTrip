package com.neungi.moyeo.views.home

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.neungi.moyeo.R
import com.neungi.moyeo.config.BaseFragment
import com.neungi.moyeo.databinding.FragmentNotificationBinding
import com.neungi.moyeo.views.MainViewModel
import com.neungi.moyeo.views.home.adapter.HomeFestivalAdapter
import com.neungi.moyeo.views.home.adapter.NotificationAdapter
import com.neungi.moyeo.views.home.viewmodel.HomeViewModel

class NotificationFragment: BaseFragment<FragmentNotificationBinding>(R.layout.fragment_notification) {

    private val viewModel: HomeViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var notificationAdapter: NotificationAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vm = viewModel
        binding.toolbarNotification.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        setAdapter()
        collectLatestFlow(viewModel.notificationHistory){ history ->
            notificationAdapter.submitList(history)
        }

    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setBnvState(false)
    }

    private fun setAdapter() {
        notificationAdapter = NotificationAdapter()
        binding.rvNotifications.adapter = notificationAdapter
        setupSwipeToDelete()

    }
    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, // 드래그 방향 (사용하지 않음)
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // 스와이프 방향
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val notification = notificationAdapter.currentList[position]
                // ViewModel을 통해 삭제 처리
                viewModel.deleteNotification(notification.id)
            }


        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvNotifications)
    }
}