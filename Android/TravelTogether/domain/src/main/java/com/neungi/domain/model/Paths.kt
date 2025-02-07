package com.neungi.domain.model

data class Path(
    val sourceScheduleId:Int,
    val targetScheduleId:Int,
    val path : List<Pair<Double,Double>>
)