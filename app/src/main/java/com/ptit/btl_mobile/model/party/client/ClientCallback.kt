package com.ptit.btl_mobile.model.party.client

import android.net.Uri

interface ClientCallback {
    fun onFileTransferProgress(name: String, progress: Float)
    fun onSync(uri: Uri, position: Long)
    fun onEnd()
}