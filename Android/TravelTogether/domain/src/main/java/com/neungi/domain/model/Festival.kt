package com.neungi.domain.model

import java.util.Date

data class Festival(
    val title : String,
    val imageUrl: String?,
    val address:String,
    val startDate: String,
    val endDate: String,
    val overView:String = "",
    var contentId:String = "0"
)
