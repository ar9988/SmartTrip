package com.neungi.moyeo.views.plan

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


val itemTouchHelperCallback = object : ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 아이템만 드래그 가능 (섹션 헤더는 제외)
        return if (viewHolder is SectionedAdapter.SectionHeaderViewHolder) {
            0 // 섹션 헤더는 움직일 수 없도록 플래그를 0으로 설정
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
        // 타겟이 섹션 헤더일 경우 이동을 허용하지 않음
        if (viewHolder is SectionedAdapter.SectionHeaderViewHolder ||
            target is SectionedAdapter.SectionHeaderViewHolder
        ) {
            return false
        }

        val adapter = recyclerView.adapter as SectionedAdapter
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        adapter.moveItem(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // 스와이프 비활성화
    }
}