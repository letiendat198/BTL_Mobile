package com.ptit.btl_mobile.model.party.utils;

import java.io.InputStream;
import java.util.Arrays;

import static java.lang.Math.min;

import android.util.Log;

import com.ptit.btl_mobile.model.party.message.Message;
import com.ptit.btl_mobile.model.party.message.MessageDecoder;

public class SocketRead {
    private static final int MAX_PAYLOAD_SIZE = SocketConfig.MAX_PAYLOAD_SIZE;
    public static Message readMessage(InputStream inStream){
        if (inStream != null){
            try{
                byte[] buf = new byte[MAX_PAYLOAD_SIZE];
                int len = inStream.read(buf, 0, 7);

                //Read until 7 bytes
                while (len < 7) {
                    int read = inStream.read(buf, len, 7 - len);
                    if (read > 0) len += read;
                }
                // Read until message is complete
                while (!Message.isComplete(Arrays.copyOfRange(buf, 0, len))) {
//                    logger.debug("Data incomplete, current buffer: {}. Actual data: {}. Expected: {}", len, len-7,MessageDecoder.getDataLength(buf));
                    // Read the rest of data or until MAX_PAYLOAD_SIZE to avoid overflow into buffer
//                    logger.debug("Try to read {} bytes more", min(MessageDecoder.getDataLength(buf) - len + 7, MAX_PAYLOAD_SIZE - len));
                    int read = inStream.read(buf, len, min(Message.getDataLength(buf) - len + 7, MAX_PAYLOAD_SIZE - len));
                    if (read > 0) len += read;
//                    if (read>0) logger.debug("Read {} bytes more", read);
                }
                Log.d("SOCKET", new String(Arrays.copyOfRange(buf, 0, 3)));
                return MessageDecoder.decode(Arrays.copyOfRange(buf, 0, len));
            }
            catch (Exception e){
                Log.e("SOCKET", "Error while reading socket", e);
            }
        }
        return null;
    }
}
