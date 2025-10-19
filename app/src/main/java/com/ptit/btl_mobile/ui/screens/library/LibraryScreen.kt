package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.SongList

@Composable
fun LibraryScreen(viewModel: LibraryViewModel = viewModel()) {
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.value.size)
    SongList(viewModel.songs.value)
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LibraryPreview() {
    LibraryScreen()
}