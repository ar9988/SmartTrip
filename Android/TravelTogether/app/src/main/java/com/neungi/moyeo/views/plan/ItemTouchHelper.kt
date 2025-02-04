package com.neungi.moyeo.views.plan

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neungi.moyeo.R
import com.neungi.moyeo.util.ListItem
import com.neungi.moyeo.views.plan.adapter.SectionedAdapter
import timber.log.Timber

fun createItemTouchHelperCallback(
    updatePosition: (scheduleId: Int, positionPath: Int) -> Unit,
    onDrag: (Boolean) -> Unit
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
        override fun isLongPressDragEnabled() = false

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = recyclerView.adapter as SectionedAdapter
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition


            // 현재 화면에 보이는 부분의 위치 저장
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val firstVisible = layoutManager.findFirstVisibleItemPosition()
//            val lastVisible = layoutManager.findLastVisibleItemPosition()

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
            //위치값 update
            val newPositionPath = (upsidePositionPath + downsidePositionPath) / 2
            updatePosition(
                (adapter.getItem(toPosition) as ListItem.Item).data.scheduleId,
                newPositionPath
            )
            adapter.updateValue(toPosition, newPositionPath)


//            layoutManager.scrollToPosition(fromPosition)

            Timber.d("onMove: $fromPosition -> $toPosition, ${((upsidePositionPath + downsidePositionPath) / 2)}")
            return true
        }


        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // 스와이프 비활성화
        }
        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    // 드래그 종료 시
                    onDrag(false)
                }
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    // 터치가 시작되었을 때
                    viewHolder?.itemView?.findViewById<ConstraintLayout>(R.id.card_schedule)?.let { card ->
                        // 드래그 중일 때의 배경색 설정
                        card.setBackgroundResource(R.drawable.round_border_dragging)  // 드래그 중일 때 사용할 배경
                    }
                    onDrag(true)
                }
            }
        }
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            // 드래그가 끝났을 때 원래 배경으로 복원
            viewHolder.itemView.findViewById<ConstraintLayout>(R.id.card_schedule)?.let { card ->
                card.setBackgroundResource(R.drawable.round_border)  // 기본 배경
            }
        }
    }
}
