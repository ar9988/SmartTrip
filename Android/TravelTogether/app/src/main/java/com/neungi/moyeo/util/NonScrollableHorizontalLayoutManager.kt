package com.neungi.moyeo.util

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NonScrollableHorizontalLayoutManager(context: Context) : LinearLayoutManager(context, HORIZONTAL, false) {
    override fun canScrollHorizontally(): Boolean = false
    override fun canScrollVertically(): Boolean = false
}