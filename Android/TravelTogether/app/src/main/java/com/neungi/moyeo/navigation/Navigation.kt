package com.neungi.moyeo.navigation

sealed class Navigation(val destination: String) {
    data object Splash : Navigation("splash")
    data object Home : Navigation("home")
    data object Plan : Navigation("plan")
    data object Planner : Navigation("planner")
    data object Album : Navigation("album")
    data object AlbumDetail : Navigation("albumdetail")
    data object Setting : Navigation("setting")
}

sealed class Route(val value: String) {
    data object Main : Navigation("main")
}