package com.ptit.btl_mobile.ui.screens.library

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.SongList

@Composable
fun LibraryScreen() {
    // Using MainActivity store owner allow view model
    // to be maintained even after user navigated away
    // TODO: CAREFULL!! HERE WE ASSUME THIS COMPOSABLE IS BOUND TO AN ACTIVITY
    // IT SHOULD BE!
    val viewModel: LibraryViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    Log.d("LIBRARY_SCREEN", "Recomposed when song list size is: " + viewModel.songs.value.size)
    SongList(viewModel.songs.value , onClick = { song ->
    })
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun LibraryPreview() {
    LibraryScreen()
}