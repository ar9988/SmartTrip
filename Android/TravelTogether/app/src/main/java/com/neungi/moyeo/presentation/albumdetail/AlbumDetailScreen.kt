package com.neungi.moyeo.presentation.albumdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.neungi.moyeo.presentation.albumdetail.viewmodel.AlbumDetailViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AlbumDetailScreen(
    navController: NavController,
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val scrollState = rememberLazyListState()
    val toolbarHeight = 300.dp
    val mapHeight = 400.dp
    val collapseHeight = 100.dp

    val uiState = viewModel.uiState

    val offset = remember {
        derivedStateOf {
            (toolbarHeight - collapseHeight).value - scrollState.firstVisibleItemScrollOffset / 1.5f
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                 // MapView
            }
            item {
                // Text("추억 앨범")
            }
        }
    }
}

@Composable
fun MapView(imageUrl: String?) {

}

@Composable
fun ImageCard(imageUrl: String) {

}