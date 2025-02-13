package com.neungi.moyeo.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OverlappingItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)

        // 첫 번째 아이템이 아닌 경우에만 왼쪽 마진을 음수값으로 설정
        if (position > 0) {
            outRect.left = -40  // 겹치는 정도를 픽셀 단위로 조절
        }
    }
}