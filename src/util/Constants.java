package util;
public class Constants {

    // Sender/Receiver start process
    public static final String SENDING = "Sending";
    public static final String RESENDING = "Resending";
    public static final String RECEIVING = "RECV";

    // Sender/Receiver condition
    public static final String SENT = "SENT";
    public static final String DROP = "DROP";
    public static final String ERR = "ERRR";
    public static final String CORRUPT = "CRPT";
    public static final String OUT_OF_SEQUENCE = "!Seq";
    public static final String RECEIVED = "RECV";

    public static final String ACK_RECEIVED = "AckR";
    public static final String DUP_ACK = "DuplAck";
    public static final String ERR_ACK = "ErrAck";
    public static final String TIMEOUT = "TimeOut";

    public static final int MAX_PACKET_SIZE = 4096;
    public static final short GOOD_CHECKSUM = 0;
    public static final short BAD_CHECKSUM = 1;
    public static final int MAX_RETRY = 6;
    public static final long TIMEOUT_MAX = 15 * 1000;

    public static final String[] STATUS_ARRAY = {ACK_RECEIVED};
}
