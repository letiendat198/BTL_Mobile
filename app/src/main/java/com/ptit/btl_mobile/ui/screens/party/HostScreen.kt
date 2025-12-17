package com.ptit.btl_mobile.ui.screens.party

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HostScreen() {
    val viewModel: HostViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    if (viewModel.clientList.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Hosting listening party on")
            Text("IP: ${viewModel.hostIp.value}")
            Text("Port: ${viewModel.server.port}")
        }
    }
    else {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("IP: ${viewModel.hostIp.value}")
                Text("Port: ${viewModel.server.port}")
            }
            Text("Guest list")
            LazyColumn {
                items(viewModel.clientList) {
                    Text(it)
                }
            }
        }
    }
}