package com.neungi.moyeo.navigation.navhost

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.neungi.moyeo.navigation.Navigation
import com.neungi.moyeo.navigation.Route
import com.neungi.moyeo.presentation.albumdetail.AlbumDetailScreen

@Composable
fun NavHost(
    navController: NavHostController = rememberNavController(),
) {

}

private fun NavGraphBuilder.mainNavigation(navController: NavHostController) {
    navigation(startDestination = Navigation.Home.destination, route = Route.Main.destination) {
        composable(Navigation.AlbumDetail.destination) {
            AlbumDetailScreen(navController = navController)
        }
    }
}