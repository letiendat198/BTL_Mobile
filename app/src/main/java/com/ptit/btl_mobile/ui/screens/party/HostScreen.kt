package com.ptit.btl_mobile.ui.screens.party

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.ThumbnailImage

@Composable
fun HostScreen(partyViewModel: ListeningPartyViewModel) {
    BackHandler {
        partyViewModel.changePartyState(PartyState.DEFAULT)
    }

    val viewModel: HostViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    if (viewModel.clientList.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ){
            Text("Hosting listening party on")
            Row {
                Text("IP: ", fontWeight = FontWeight.SemiBold)
                Text(viewModel.hostIp.value)
            }
            Row {
                Text("Port: ", fontWeight = FontWeight.SemiBold)
                Text("${viewModel.server.port}")
            }
        }
    }
    else {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column (
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Host info:", fontWeight = FontWeight.SemiBold)
                Row {
                    Text("IP: ", fontWeight = FontWeight.SemiBold)
                    Text(viewModel.hostIp.value)
                }
                Row {
                    Text("Port: ", fontWeight = FontWeight.SemiBold)
                    Text("${viewModel.server.port}")
                }
            }
            Text("Guest list:", fontWeight = FontWeight.SemiBold)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                items(viewModel.clientList) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                            .fillMaxWidth()
                    ) {
                        ThumbnailImage(
                            imageUri = null,
                            isCircle = true,
                            modifier = Modifier.height(50.dp).aspectRatio(1f)
                        )
                        Text(it)
                    }
                }
            }
        }
    }
}