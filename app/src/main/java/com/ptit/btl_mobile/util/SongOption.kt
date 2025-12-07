package com.ptit.btl_mobile.util

import androidx.compose.runtime.Composable
import com.ptit.btl_mobile.model.database.SongWithArtists

data class SongOption(
    val title: String,
    val icon: (@Composable () -> Unit)? = null,
    val onClick: (song: SongWithArtists) -> Unit = {}
)