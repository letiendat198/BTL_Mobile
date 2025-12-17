package com.ptit.btl_mobile.model.party.message;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

// A Message object should only be constructed by MessageDecoder or MessageBuilder
// MessageDecoder will attempt to create Message object from byte[]
// MessageBuilder will attempt to create Message object with parameters provided
public class Message {
    MessageType.MESSAGE messageType;
    byte[] rawData;
    String decodedData;
    boolean haveData = false;
    boolean haveHeaders = false;
    HashMap<String, String> messageHeader = new HashMap<>();
    int messageLength; // Actually data length

    public static boolean isComplete (byte[] raw){
        if (raw.length < 7) return false;
        byte[] dataLength = Arrays.copyOfRange(raw, 3 , 7);
        int len = (int)Long.parseLong(new String(dataLength), 16);
        return raw.length - 7 == len;
    }

    public static int getDataLength (byte[] raw){
        byte[] dataLength = Arrays.copyOfRange(raw, 3 , 7);
        return (int)Long.parseLong(new String(dataLength), 16);
    }

    // Create byte[] from Message object => Use to send over socket
    public byte[] toByteArray(boolean isPretty){
        ByteArrayOutputStream message = new ByteArrayOutputStream();
        try{
            message.write(MessageType.toString(this.messageType).getBytes());
            if (this.haveHeaders && this.messageType != MessageType.MESSAGE.BIN) {
                StringBuilder headerString = new StringBuilder();
                for (String key: this.messageHeader.keySet()){
                    headerString.append(key).append(":").append(this.messageHeader.get(key)).append("\n");
                }
                byte[] base64Header = Base64.getEncoder().encode(headerString.toString().getBytes());
                String hexLength = Integer.toHexString(base64Header.length);
                StringBuilder dataLength = new StringBuilder();
                for (int i=0;i<4-hexLength.length();i++){
                    dataLength.append("0");
                }
                dataLength.append(hexLength);
                message.write(dataLength.toString().getBytes());
                message.write(base64Header);
            }
            else if (this.messageType == MessageType.MESSAGE.BIN && this.rawData != null){
                String hexLength = Integer.toHexString(this.rawData.length);
                StringBuilder dataLength = new StringBuilder();
                for (int i=0;i<4-hexLength.length();i++){
                    dataLength.append("0");
                }
                dataLength.append(hexLength);
                message.write(dataLength.toString().getBytes());
                message.write(this.rawData);
            }
            else{
                message.write("0000".getBytes());
            }
            if (isPretty) message.write("\n\r".getBytes());
        }
        catch (IOException ioe){
            Log.e("MESSAGE", ioe.getMessage());
        }
        return message.toByteArray();
    }

    public MessageType.MESSAGE getMessageType() {
        return messageType;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public String getDecodedData() {
        return decodedData;
    }

    public boolean isHaveData() {
        return haveData;
    }

    public boolean isHaveHeaders() {
        return haveHeaders;
    }

    public HashMap<String, String> getMessageHeader() {
        return messageHeader;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public void setMessageType(MessageType.MESSAGE messageType) {
        this.messageType = messageType;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
        if (this.messageLength >0) this.haveData = true;
    }

    public void setDecodedData(String decodedData) {
        this.decodedData = decodedData;
    }

    public void setMessageHeader(HashMap<String, String> messageHeader) {
        this.messageHeader = messageHeader;
        this.haveHeaders = true;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

}
