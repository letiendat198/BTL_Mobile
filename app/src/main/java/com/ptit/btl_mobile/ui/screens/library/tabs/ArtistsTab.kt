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
import com.ptit.btl_mobile.ui.screens.library.ArtistWithInfo

@Composable
fun ArtistsTab(
    onNavToArtistDetail: (artist: ArtistWithInfo) -> Unit,
    viewModel: LibraryViewModel
) {
    val artists = viewModel.artistsWithInfo

    if (artists.isEmpty()) {
        EmptyState(message = "No artist found")
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
                        onNavToArtistDetail(artistInfo)
                    }
                )
            }
        }
    }
}