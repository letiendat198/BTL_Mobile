package com.ptit.btl_mobile.model.party.server

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ptit.btl_mobile.model.party.message.MessageBuilder
import com.ptit.btl_mobile.model.party.message.MessageType
import com.ptit.btl_mobile.model.party.utils.SocketRead
import java.net.ServerSocket
import kotlin.concurrent.thread

class Server(val context: Context) {
    private val clientMap  = mutableMapOf<String, ClientHandler>()
    var port: Int? = null
    private var onChange: (Map<String, ClientHandler>) -> Unit = {}

    fun listen() {
        val server = ServerSocket(0)
        Log.d("SERVER", "Server is running on port" + server.localPort)
        port = server.localPort

        thread {
            while (true) {
                val socket = server.accept()
                val outStream = socket.outputStream

                val passiveMessage = SocketRead.readMessage(socket.inputStream)
                if (passiveMessage.messageType == MessageType.MESSAGE.PAS) {
                    val name = passiveMessage.messageHeader["name"]

                    if (!name.isNullOrEmpty()) {
                        val handler = ClientHandler(socket, context)
                        clientMap.put(name, handler)
                        outStream.write(
                            MessageBuilder()
                                .addType(MessageType.MESSAGE.ACK)
                                .build()
                        )
                        outStream.flush()

                        onChange(clientMap)
                        continue
                    }
                }
                else {
                    Log.d("SERVER", "WARNING: Initial message not passive. Please restart connection")
                }
                outStream.write(
                    MessageBuilder()
                        .addType(MessageType.MESSAGE.RFS)
                        .build()
                )
                outStream.flush()
                // TODO: WARNING
                socket.close()
            }
        }
    }

    suspend fun syncAll(uri: Uri, position: Long) {
        Log.d("SERVER", "Sync all target with ${uri}, position: ${position}")
        val pendingDelete = mutableListOf<String>()
        clientMap.forEach { (name, handler) ->
            val status = handler.synchronize(uri, position)
            // If can't sync with a client, assume it's dead => Remove
            if (status == false) {
                Log.d("SERVER", "Can't sync with client $name")
                pendingDelete.add(name)
            }
        }
        pendingDelete.forEach {
            clientMap.remove(it)
        }
        onChange(clientMap)
        Log.d("SERVER", "Sync completed for all target")
    }

    fun setOnChangeCallback(callback: (Map<String, ClientHandler>) -> Unit) {
        onChange = callback
    }
}