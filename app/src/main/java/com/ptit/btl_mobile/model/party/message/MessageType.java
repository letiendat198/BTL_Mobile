package com.ptit.btl_mobile.model.party.message;

public class MessageType {
    public enum MESSAGE {
        // Control types
        EOF, //END OF FILE
        END, //END
        BGN, //BEGIN
        BIN, //BINARY DATA
        PAS, //PASSIVE MODE (Request target server to create a client instance to connect to self server)
        SYN, // SYNC (Sync client and server track and progress)

        // Status types
        ACK, //ACKNOWLEDGE
        NSM, //NO SUCH MESSAGE
        RFS, //REFUSE
        COM, //COMPLETE
    }

    public static MESSAGE parseType(String message) {
        if (message.equalsIgnoreCase("ACK")) return MESSAGE.ACK;
        else if (message.equalsIgnoreCase("COM")) return MESSAGE.COM;
        else if (message.equalsIgnoreCase("EOF")) return MESSAGE.EOF;
        else if (message.equalsIgnoreCase("END")) return MESSAGE.END;
        else if (message.equalsIgnoreCase("BGN")) return MESSAGE.BGN;
        else if (message.equalsIgnoreCase("BIN")) return MESSAGE.BIN;
        else if (message.equalsIgnoreCase("PAS")) return MESSAGE.PAS;
        else if (message.equalsIgnoreCase("RFS")) return MESSAGE.RFS;
        else if (message.equalsIgnoreCase("SYN")) return MESSAGE.SYN;
        else return MESSAGE.NSM;
    }

    public static String toString(MESSAGE message) {
        switch (message) {
            case ACK:
                return "ACK";
            case COM:
                return "COM";
            case EOF:
                return "EOF";
            case END:
                return "END";
            case BGN:
                return "BGN";
            case BIN:
                return "BIN";
            case PAS:
                return "PAS";
            case RFS:
                return "RFS";
            case NSM:
                return "NSM";
            case SYN:
                return "SYN";
        };
        return "";
    }
}
