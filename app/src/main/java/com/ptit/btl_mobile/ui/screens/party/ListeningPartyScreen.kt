package com.ptit.btl_mobile.ui.screens.party

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ptit.btl_mobile.ui.components.TopAppBarContent

@Composable
fun ListeningPartyScreen(
    onSetTopAppBar: (TopAppBarContent) -> Unit
) {
    val viewModel: ListeningPartyViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    onSetTopAppBar(TopAppBarContent(
        title = "Listening Party"
    ))

    val partyState by viewModel.currentPartyState

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when (partyState) {
            PartyState.DEFAULT -> DefaultScreen(viewModel)
            PartyState.JOIN -> JoinScreen(viewModel)
            PartyState.JOINED -> JoinedScreen(viewModel)
            PartyState.HOST -> HostScreen(viewModel)
        }
    }

}

@Composable
fun DefaultScreen(viewModel: ListeningPartyViewModel) {
    val joinViewModel: JoinViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
         modifier = Modifier.fillMaxSize()
    ) {
        Text("Would you like to")
        FilledTonalButton (onClick = {
            if (joinViewModel.client.socket == null) {
                viewModel.changePartyState(PartyState.JOIN)
            }
            else {
                viewModel.changePartyState(PartyState.JOINED)
            }
        }) {
            Text("Join")
        }
        Button(onClick = {
            viewModel.changePartyState(PartyState.HOST)
        }) {
            Text("Host")
        }
    }
}