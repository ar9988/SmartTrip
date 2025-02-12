package com.neungi.moyeo.views.home.viewmodel

import androidx.annotation.DrawableRes

data class Quote(
    val text: String,
    val author: String,
    val source: String? = null,
    @DrawableRes val backgroundImageRes: Int
)