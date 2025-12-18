package com.ptit.btl_mobile.model.party.client

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.ptit.btl_mobile.model.party.message.MessageBuilder
import com.ptit.btl_mobile.model.party.message.MessageType
import com.ptit.btl_mobile.model.party.utils.SocketConfig
import com.ptit.btl_mobile.model.party.utils.SocketRead
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket
import kotlin.concurrent.thread

class Client(val context: Context) {
    var socket: Socket? = null

    fun connect(ip: String, port: Int): Boolean {
        try {
            Log.d("CLIENT", "Connecting...")
            socket = Socket(ip, port)
            return requestPassiveMode(Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME))
        }
        catch (e: Exception) {
            Log.e("CLIENT", "Error when connecting to server", e)
            return false
        }
    }

    private fun requestPassiveMode(username: String): Boolean {
        socket?.let {
            Log.d("CLIENT", "Requesting passive mode")
            val inStream = BufferedInputStream(it.inputStream, SocketConfig.MAX_PAYLOAD_SIZE)
            val outStream = BufferedOutputStream(it.outputStream, SocketConfig.MAX_PAYLOAD_SIZE)

            try {
                outStream.write(
                    MessageBuilder()
                        .addType(MessageType.MESSAGE.PAS)
                        .addHeader("name", username)
                        .build()
                )
                outStream.flush()
                val message = SocketRead.readMessage(inStream)
                if (message.messageType == MessageType.MESSAGE.ACK) {
                   return true
                }
            }
            catch (e: Exception){
                Log.e("CLIENT", "Error on establish passive mode with server", e)
            }
        }
        return false
    }

    fun listen(callback: ClientCallback) {
        thread {
            socket?.let {
                try {
                    val inStream = BufferedInputStream(it.inputStream, SocketConfig.MAX_PAYLOAD_SIZE)
                    val outStream = BufferedOutputStream(it.outputStream, SocketConfig.MAX_PAYLOAD_SIZE)
                    val handler = RequestHandler(context, inStream, outStream, callback)
                    while (true) {
                        if (it.isClosed) break

                        val message = SocketRead.readMessage(inStream)

                        when (message.messageType) {
                            MessageType.MESSAGE.EOF -> handler.onEOF()
                            MessageType.MESSAGE.END -> handler.onEnd()
                            MessageType.MESSAGE.BGN -> handler.onBegin(message)
                            MessageType.MESSAGE.BIN -> handler.onBinary()
                            MessageType.MESSAGE.SYN -> handler.onSync(message)
                            else -> Log.d("CLIENT", "Client won't handle status type message: " + message.messageType.toString())
                        }

                        if (handler.terminated) break
                    }
                    it.close()
                }
                catch (e: Exception) {
                    Log.e("CLIENT", "Error while listening", e)
//                    it.close()
                }
            }?:Log.e("CLIENT", "CAN'T LISTEN WHEN THERE IS NO CONNECTION")
        }
    }
}