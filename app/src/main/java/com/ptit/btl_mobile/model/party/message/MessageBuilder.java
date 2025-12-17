package com.ptit.btl_mobile.model.party.message;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public class MessageBuilder {

    private Message message = new Message();
    private boolean isPretty = false;
    private HashMap<String, String> header = new HashMap<>();

    public MessageBuilder addType(MessageType.MESSAGE messageType){
        message.setMessageType(messageType);
        return this;
    }
    public MessageBuilder addHeader(String key, String value){
        header.put(key, value);
        message.setMessageHeader(header);
        return this;
    }

    public MessageBuilder addRawBytes(byte[] bytes){
        message.setRawData(bytes);
        return this;
    }

    public MessageBuilder pretty(boolean isPretty){
        this.isPretty = isPretty;
        return this;
    }

    public byte[] build(){
        Log.d("MESSAGE_BUILDER", new String(message.toByteArray(false)));
        return message.toByteArray(isPretty);
    }


}
