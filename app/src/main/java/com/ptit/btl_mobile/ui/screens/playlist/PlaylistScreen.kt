package com.ptit.btl_mobile.ui.screens.playlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.model.database.Playlist
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.mutableListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    onBackClick: () -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {}
) {
    val playlists = remember { mutableListOf<Playlist>() } // TODO: PUT IN VIEWMODEL. PLACEHOLDER ONLY
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlists", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistCard(playlist = playlist, onClick = { onPlaylistClick(playlist) })
            }
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_music_sample),
            contentDescription = playlist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = playlist.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        playlist.dateCreated?.let {
            Text(
                text = dateFormat.format(it),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaylistScreenPreview() {
    val samplePlaylists = listOf(
        Playlist(1, "Relax Vibes", null, Date()),
        Playlist(2, "Workout Mix", null, Date()),
        Playlist(3, "Chill Evening", null, Date())
    )

    BTL_MobileTheme {
        PlaylistScreen()
    }
}
