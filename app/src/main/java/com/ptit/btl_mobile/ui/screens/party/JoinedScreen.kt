package com.ptit.btl_mobile.ui.screens.party

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JoinedScreen() {
    val viewModel: JoinViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    Column {
        Text("Joined a listening party")

        Text("Pending files")
        Text(viewModel.currentFile.value)
        LinearProgressIndicator(
            progress = { viewModel.currentProgress.value }
        )
    }
}