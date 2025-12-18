package com.ptit.btl_mobile.ui.screens.party

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.application
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JoinScreen(partyViewModel: ListeningPartyViewModel) {
    var ip by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    val pattern = remember { Regex("^\\d*\$") }

    var isLoading by remember { mutableStateOf(false) }

    val viewModel: JoinViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity
    )

    BackHandler {
        partyViewModel.changePartyState(PartyState.DEFAULT)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Please enter host IP and port")
        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = {Text("IP address")}
        )
        OutlinedTextField(
            label = { Text("Port") },
            value = port,
            onValueChange = { value: String ->
                if (value.matches(pattern))
                    port = value
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Button(onClick = {
            isLoading = true
            viewModel.connectToHost(ip, port.toIntOrNull()?:0) { status ->
                isLoading = false
                if (!status) {
                    Toast.makeText(
                        viewModel.application,
                        "Connect to party host failed!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else { partyViewModel.changePartyState(PartyState.JOINED) }
            }
        } ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(20.dp).aspectRatio(1f)
                    )
                    Text("Connecting...")
                }
                else Text("Connect")
            }
        }
    }
}