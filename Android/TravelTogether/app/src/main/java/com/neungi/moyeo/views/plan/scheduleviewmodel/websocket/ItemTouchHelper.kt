package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

fun createItemTouchHelperCallback(
    onItemMove: (fromPosition: Int, toPosition: Int) -> Unit
): ItemTouchHelper.Callback {
    return object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (viewHolder is SectionedAdapter.SectionHeaderViewHolder) {
                0 // 섹션 헤더는 드래그 불가
            } else {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                makeMovementFlags(dragFlags, 0)
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter as SectionedAdapter
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition

            // 드래그 제한 조건 처리
            val targetItem = adapter.getItem(toPosition)
            if (targetItem is ListItem.SectionHeader && targetItem.title == "1일차") {
                return false // 1일차 섹션으로 이동 금지
            }

            // 어댑터 내부에서 아이템 이동 처리
            adapter.moveItem(fromPosition, toPosition)

            // 이동 이벤트를 람다를 통해 ViewModel로 전달
            onItemMove(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프 비활성화
        }
    }
}