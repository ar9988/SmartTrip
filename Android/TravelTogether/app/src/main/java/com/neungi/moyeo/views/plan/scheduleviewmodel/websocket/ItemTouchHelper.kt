package com.neungi.moyeo.views.plan.scheduleviewmodel.websocket

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

fun createItemTouchHelperCallback(
    updatePosition: (scheduleId: Int, positionPath: Int) -> Unit
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
            if (targetItem is ListItem.SectionHeader && targetItem.data.title == "1일차") {
                return false // 1일차 섹션으로 이동 금지
            }

            // 아이템 이동 처리
            adapter.moveItem(fromPosition, toPosition)
            Timber.d("onMove: $fromPosition -> $toPosition")
            val upsideItem =
                if (toPosition == 0) adapter.getItem(0) else adapter.getItem(toPosition - 1)
            val upsidePositionPath =
                if (upsideItem is ListItem.SectionHeader) upsideItem.data.positionPath else (upsideItem as ListItem.Item).data.positionPath

            val downsidePositionPath = if (toPosition == adapter.itemCount - 1) {
                val tmp = adapter.getItem(toPosition - 1)
                if (tmp is ListItem.SectionHeader) {
                    // SectionHeader 처리
                    when (val fromItem = adapter.getItem(fromPosition)) {
                        is ListItem.SectionHeader -> fromItem.data.positionPath + 1000
                        is ListItem.Item -> fromItem.data.positionPath + 1000
                        else -> throw IllegalStateException("Unexpected item type")
                    }
                } else if (tmp is ListItem.Item) {
                    tmp.data.positionPath + 1000
                } else {
                    throw IllegalStateException("Unexpected item type")
                }
            } else {
                val tmp = adapter.getItem(toPosition + 1)
                if (tmp is ListItem.SectionHeader) {
                    // SectionHeader 처리
                    ((adapter.getItem(toPosition + 1) as ListItem.SectionHeader).data.positionPath)
                } else if (tmp is ListItem.Item) {
                    // Item 처리
                    (tmp as ListItem.Item).data.positionPath
                } else {
                    // 다른 타입 처리 (예: ListItem이 아닌 경우)
                    throw IllegalStateException("Unexpected item type")
                }
            }
            val newPositionPath = (upsidePositionPath + downsidePositionPath) / 2
            updatePosition(
                (adapter.getItem(toPosition) as ListItem.Item).data.scheduleId,
                newPositionPath
            )
            adapter.updateValue(toPosition, newPositionPath)
            Timber.d("onMove: $fromPosition -> $toPosition, ${((upsidePositionPath + downsidePositionPath) / 2)}")
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프 비활성화
        }
    }
}
