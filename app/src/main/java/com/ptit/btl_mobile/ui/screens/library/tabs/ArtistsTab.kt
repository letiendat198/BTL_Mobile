package com.ptit.btl_mobile.ui.screens.library.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ptit.btl_mobile.ui.screens.library.LibraryViewModel
import com.ptit.btl_mobile.ui.screens.library.components.ArtistItem
import com.ptit.btl_mobile.ui.components.EmptyState

@Composable
fun ArtistsTab(
    navController: NavController,
    viewModel: LibraryViewModel
) {
    val artists = viewModel.artistsWithInfo

    if (artists.isEmpty()) {
        EmptyState(message = "Chưa có nghệ sĩ nào")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(artists.size) { index ->
                val artistInfo = artists[index]
                ArtistItem(
                    artistInfo = artistInfo,
                    onClick = {
                        navController.navigate("library/artist/${artistInfo.artist.artistId}")
                    }
                )
            }
        }
    }
}