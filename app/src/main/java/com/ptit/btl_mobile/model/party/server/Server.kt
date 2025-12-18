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

    fun listen(onChange: (Map<String, ClientHandler>) -> Unit = {}) {
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
        clientMap.forEach { (name, handler) ->
            handler.synchronize(uri, position)
        }
    }
}