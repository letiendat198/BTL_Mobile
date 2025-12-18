package com.ptit.btl_mobile.ui.screens.party

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.ptit.btl_mobile.model.media.MediaControllerStore
import com.ptit.btl_mobile.model.party.client.Client
import com.ptit.btl_mobile.model.party.client.ClientCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JoinViewModel(val context: Application): AndroidViewModel(context) {
    // Client lives as long as this view model
    val client = Client(context)
    val currentFile = mutableStateOf("")
    val currentProgress = mutableFloatStateOf(0f)
    var currentUri: Uri? = null

    // When server pushes info to client, use this callback to influence the UI and media controller
    val callback = object: ClientCallback  {
        override fun onFileTransferProgress(name: String, progress: Float) {
            currentFile.value = name
            currentProgress.floatValue = progress
        }

        override fun onSync(uri: Uri, position: Long) {
            val controller = MediaControllerStore.mediaController

            if (currentUri != uri) {
                controller?.clearMediaItems()
                controller?.setMediaItem(MediaItem.fromUri(uri))
                controller?.prepare()
                controller?.play()

                currentUri = uri
            }
            controller?.seekTo(position)
        }

        override fun onEnd() {

        }

    }

    fun connectToHost(ip: String, port: Int, onConnected: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val connected = client.connect(ip, port)
            if (connected) client.listen(callback)
            withContext(Dispatchers.Main) {
                onConnected(connected)
            }
        }
    }
}