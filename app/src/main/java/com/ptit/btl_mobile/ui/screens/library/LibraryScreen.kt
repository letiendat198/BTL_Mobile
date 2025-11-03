package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ptit.btl_mobile.ui.components.SongList
import com.ptit.btl_mobile.ui.screens.library.tabs.AlbumsTab
import com.ptit.btl_mobile.ui.screens.library.tabs.ArtistsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    navController: NavController = rememberNavController()
) {
    val viewModel: LibraryViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.size)

    // Persist song list state in Library view model
    viewModel.listState = viewModel.listState ?: rememberLazyListState()

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Bài hát", "Albums", "Nghệ sĩ")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Thư viện") }
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> {
                    // Tab Bài hát - CODE CŨ
                    SongList(
                        songs = viewModel.songs,
                        customState = viewModel.listState
                    )
                }
                1 -> {
                    // Tab Albums - MỚI
                    AlbumsTab(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
                2 -> {
                    // Tab Nghệ sĩ - MỚI
                    ArtistsTab(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LibraryPreview() {
    LibraryScreen()
}