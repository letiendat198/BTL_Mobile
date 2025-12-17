package com.ptit.btl_mobile.model.party.server

import android.content.Context
import android.icu.text.ConstrainedFieldPosition
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.ptit.btl_mobile.model.party.message.MessageBuilder
import com.ptit.btl_mobile.model.party.message.MessageType
import com.ptit.btl_mobile.model.party.message.MessageType.MESSAGE
import com.ptit.btl_mobile.model.party.utils.SocketConfig.MAX_PAYLOAD_SIZE
import com.ptit.btl_mobile.model.party.utils.SocketRead
import com.ptit.btl_mobile.util.getFileInfoFromUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.Socket
import java.util.Arrays
import java.util.Date
import kotlin.concurrent.thread


class ClientHandler(val socket: Socket, val context: Context) {
    val inStream = socket.inputStream
    val outStream = socket.outputStream

    private fun sendFile(uri: Uri): Boolean {
        val fileInfo = getFileInfoFromUri(context, uri)
        if (fileInfo == null) return false

        val fileName = fileInfo.filename
        val size = fileInfo.size

        outStream.write(
            MessageBuilder().addType(MESSAGE.BGN)
                .addHeader("fileName", fileName)
                .addHeader("size", size.toString())
                .build()
        )
        outStream.flush()
        val mess = SocketRead.readMessage(inStream)
        Log.d("CLIENT", String.format(
            "Type: {} - Length: {} - Data: {}",
            mess.getMessageType(),
            mess.getMessageLength(),
            mess.getRawData()
        ))
        if (mess != null && mess.getMessageType() === MESSAGE.ACK) {
            val fileStream = context.contentResolver.openInputStream(uri)!!
            var sent: Long = 0
            while (true) {  //Read until EOF is sent => Guarantee server will stop
                val buf = ByteArray(MAX_PAYLOAD_SIZE - 7)
                val len = fileStream.read(buf)
                if (len > 0) {
                    val message = MessageBuilder().addType(MESSAGE.BIN)
                        .addRawBytes(Arrays.copyOfRange(buf, 0, len)).build()
                    outStream.write(message)
                    sent += len.toLong()
                } else {
                    outStream.write(MessageBuilder().addType(MESSAGE.EOF).build())
                    outStream.flush()
                    break
                }
            }
            // If server exit on its own => COM then RFS (in response to EOF)
            // Else, server exit via EOF => ACK
            val conf = SocketRead.readMessage(inStream)
            if (conf == null || conf.getMessageType() !== MESSAGE.COM) {
                return false
            }
            SocketRead.readMessage(inStream) // Exhaust RFS
            fileStream.close()
            return true
        }
        return false
    }

    fun synchronize(uri: Uri, position: Long): Boolean {
        val fileInfo = getFileInfoFromUri(context, uri)
        if (fileInfo == null) return false

        try {
            while (true) {
                outStream.write(
                    MessageBuilder()
                        .addType(MESSAGE.SYN)
                        .addHeader("name", fileInfo.filename)
                        .addHeader("position", position.toString())
                        .addHeader("timestamp", SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()))
                        .build()
                )
                outStream.flush()

                val message = SocketRead.readMessage(inStream)
                if (message.messageType == MESSAGE.ACK) return true

                val sendStatus = sendFile(uri)
                runBlocking { // Delay to wait for client to stabilize?
                    delay(1000)
                }
                if (!sendStatus) return false // If can't send file. Nothing can be done. Return
            }
        }
        catch (e: Exception){
            Log.e("CLIENT_HANDLER", "Error while synchronize", e)
        }
        return false
    }

    fun close() {
        outStream.write(MessageBuilder().addType(MESSAGE.END).build())
        outStream.flush()
        val message = SocketRead.readMessage(inStream)
        if (message.getMessageType() === MESSAGE.ACK) {
            socket.close()
        }
    }
}