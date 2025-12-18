package com.ptit.btl_mobile.model.party.client

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ptit.btl_mobile.model.party.message.Message
import com.ptit.btl_mobile.model.party.message.MessageBuilder
import com.ptit.btl_mobile.model.party.message.MessageType
import com.ptit.btl_mobile.model.party.utils.SocketRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.internal.toLongOrDefault
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class RequestHandler(
    val context: Context,
    val inStream: InputStream,
    val outStream: OutputStream,
    val callback: ClientCallback
) {
    var terminated = false
    val PARTY_TEMP_FOLDER = "partyTemp"
    fun onBegin(message: Message) {
        val fileName: String? = message.messageHeader.get("fileName")
        val size: Long = message.messageHeader.get("size")?.toLong() ?: 0

        outStream.write(MessageBuilder().addType(MessageType.MESSAGE.ACK).build())
        outStream.flush()

        val outFile = File(context.getDir(PARTY_TEMP_FOLDER, Context.MODE_PRIVATE), fileName?:"temp.mp3")
        outFile.createNewFile()

        var written: Long = 0
        val fileOut = outFile.outputStream()
        Log.d("HANDLER", "Enter file transfer mode")
        while (written < size) {
            val message = SocketRead.readMessage(inStream)

            if (message.getMessageType() == MessageType.MESSAGE.EOF) {
                outStream.write(MessageBuilder().addType(MessageType.MESSAGE.ACK).build())
                outStream.flush()
                Log.d("HANDLER", "EOF sent by client")
                break
            } else if (message.getMessageType() == MessageType.MESSAGE.BIN) {
                if (message.isHaveData()) {
                    val fileData = message.getRawData()
                    fileOut.write(fileData)
                    written += message.getMessageLength().toLong()
                }
            } else {
                outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
                outStream.flush()
                Log.d("HANDLER",
                    message.getMessageType().toString() + " request won't be handled in file transfer mode"
                )
            }
            Log.d("HANDLER", String.format("Written: %d. Expected: %d", written, size))
            GlobalScope.launch(Dispatchers.Main) {
                callback.onFileTransferProgress(fileName?:"", written.toFloat() / size.toFloat())
            }
        }
        Log.d("HANDLER", "Done writing to file, written " + written)
        outStream.write(MessageBuilder().addType(MessageType.MESSAGE.COM).build())
        outStream.flush()
        fileOut.close()
    }

    fun onBinary() {
        outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
        outStream.flush()
        Log.d("HANDLER","BIN request outside of file transfer mode")
    }

    fun onEnd() {
        Log.d("HANDLER","END request received")
        outStream.write(MessageBuilder().addType(MessageType.MESSAGE.ACK).build())
        outStream.flush()
        var len = inStream.read()
        while (len != -1) {
            Log.d("HANDLER","Waiting for close on client side")
            len = inStream.read()
        }
        terminated = true
        Log.d("HANDLER","Closing socket")
        callback.onEnd()
    }

    fun onEOF() {
        outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
        outStream.flush()
        Log.d("HANDLER","EOF request outside of file transfer mode")
    }

    fun onSync(message: Message) {
        if (!message.isHaveHeaders()) {
            outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
            Log.d("HANDLER", "SYN request without headers is unacceptable")
        }
        else {
            val fileName: String? = message.getMessageHeader().get("name")
            val position: Long = message.messageHeader.get("position")!!.toLongOrDefault(0)
            // TODO: Add time difference to position
            if (fileName.isNullOrEmpty()) {
                outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
            }
            else {
                Log.d("HANDLER", "Host player is requesting file: " + fileName)
                val file = File(context.getDir(PARTY_TEMP_FOLDER, Context.MODE_PRIVATE), fileName)
                if (file.exists()) {
                    GlobalScope.launch(Dispatchers.Main) {
                        callback.onSync(Uri.fromFile(file), position)
                    }
                    outStream.write(MessageBuilder().addType(MessageType.MESSAGE.ACK).build())
                }
                else outStream.write(MessageBuilder().addType(MessageType.MESSAGE.RFS).build())
            }
        }
        outStream.flush()
    }
}