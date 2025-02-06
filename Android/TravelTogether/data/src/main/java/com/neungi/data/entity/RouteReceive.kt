package com.neungi.data.entity

import com.neungi.domain.model.Route

data class RouteReceive(
    val tripId :String,
    val routes : List<Route>
)