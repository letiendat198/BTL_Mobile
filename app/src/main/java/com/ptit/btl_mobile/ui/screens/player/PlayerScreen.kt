package com.ptit.btl_mobile.ui.screens.player

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.R
import com.ptit.btl_mobile.ui.components.PlaybackControl
import com.ptit.btl_mobile.ui.theme.BTL_MobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen() {
    val viewModel = viewModel<PlayerViewModel>(viewModelStoreOwner = LocalActivity.current as ComponentActivity)
    var seekPosition by remember { mutableFloatStateOf(0f) }

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(40.dp)) {
            Box(contentAlignment = Alignment.Center,
            modifier = Modifier
            .weight(4F)
            .fillMaxWidth()) {
                Text("IMAGE PLACEHOLDER")
            }
            Box(modifier = Modifier
            .weight(1F)
            .fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row { 
                        Column {
                            Text("Song title")
                            Text("Artists")
                        }

                    }
                    Slider(
                        value = seekPosition,
                        onValueChange = {seekPosition = it},
                        onValueChangeFinished = {},
                        valueRange = 0f..30f
                    )
                }

            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier
            .weight(1F)
            .fillMaxWidth()) {
                PlaybackControl(viewModel, modifier = Modifier.fillMaxWidth())
            }
            Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier
            .weight(0.5F)
            .fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(painter = painterResource(R.drawable.lyrics),
                            contentDescription = "Lyrics",
                            modifier = Modifier.fillMaxSize())
                    }
                    IconButton(
                        onClick = {},
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Icon(painter = painterResource(R.drawable.queue_music),
                            contentDescription = "Queue music",
                            modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    BTL_MobileTheme {
        PlayerScreen()
    }
}