package com.ptit.btl_mobile.model.party.message;

import android.util.Log;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class MessageDecoder {
    public static Message decode(byte[] data) {
        Message message = new Message();

        String messType = new String(Arrays.copyOfRange(data, 0, 3));
        message.setMessageType(MessageType.parseType(messType));

        String dataLenHex = new String(Arrays.copyOfRange(data, 3, 7));
        int dataLen = (int) Long.parseLong(dataLenHex, 16);
        message.setMessageLength(dataLen);

        byte[] rawData = Arrays.copyOfRange(data,7, data.length);
        message.setRawData(rawData);

        if (message.getMessageType() != MessageType.MESSAGE.BIN && dataLen>0){
            try {
                String trimmedData = new String(rawData).trim();
                String decodedData = new String(Base64.getDecoder().decode(trimmedData.getBytes())).trim();
                message.setDecodedData(decodedData);
                String[] parts = decodedData.split("\n");
                HashMap<String, String> headers = new HashMap<>();
                for (String part: parts){
                    String[] splitted = part.split(":");
                    String key = splitted[0].trim();
                    String value = splitted[1].trim();
                    headers.put(key, value);
                }
                message.setMessageHeader(headers);
                Log.d("MESSAGE_DECODER", message.getMessageHeader().toString());
            }
            catch (Exception e){
                Log.e("MESSAGE_DECODER", e.getMessage());
            }
        }
        Log.d("MESSAGE_DECODER", new String(message.toByteArray(false)));
        return message;
    }
}
