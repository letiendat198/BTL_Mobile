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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme

data class Playlist(
    val id: Int,
    val title: String,
    val author: String,
    val thumbnailRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen() {
    val playlists = listOf(
        Playlist(1, "Believer", "Imagine Dragons", R.drawable.ic_music_sample),
        Playlist(2, "Shortwave", "Ryan Grigdry", R.drawable.ic_music_sample),
        Playlist(3, "Dream On", "Roger Terry", R.drawable.ic_music_sample),
        Playlist(4, "Origins", "Imagine Dragons", R.drawable.ic_music_sample),
        Playlist(5, "Gravity", "Olivia", R.drawable.ic_music_sample),
        Playlist(6, "Moonlight", "Alan Walker", R.drawable.ic_music_sample)
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* TODO: navigate back */ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1F1F1F)
                    )
                }

                IconButton(onClick = { /* TODO: open settings/filter */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Settings",
                        tint = Color(0xFF1F1F1F)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9))
                .padding(innerPadding)
        ) {
            Text(
                text = "Playlists",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(playlists) { playlist ->
                    PlaylistCard(playlist = playlist)
                }
            }
        }
    }
}

@Composable
fun PlaylistCard(playlist: Playlist) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: navigate to playlist detail */ }
    ) {
        Image(
            painter = painterResource(id = playlist.thumbnailRes),
            contentDescription = playlist.title,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = playlist.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F1F1F),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = playlist.author,
            fontSize = 13.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PlaylistScreenPreview() {
    BTL_MobileTheme {
        PlaylistScreen()
    }
}
