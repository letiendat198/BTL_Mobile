package com.ptit.btl_mobile.ui.screens.library.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ptit.btl_mobile.ui.screens.library.LibraryViewModel
import com.ptit.btl_mobile.ui.screens.library.components.AlbumCard
import com.ptit.btl_mobile.ui.components.EmptyState
import com.ptit.btl_mobile.ui.screens.library.AlbumWithInfo

@Composable
fun AlbumsTab(
    onNavToAlbumDetail: (album: AlbumWithInfo) -> Unit,
    viewModel: LibraryViewModel
) {
    val albums = viewModel.albumsWithInfo

    if (albums.isEmpty()) {
        EmptyState(message = "No album found")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(albums.size) { index ->
                val albumInfo = albums[index]
                AlbumCard(
                    albumInfo = albumInfo,
                    onClick = {
                        onNavToAlbumDetail(albumInfo)
                    }
                )
            }
        }
    }
}