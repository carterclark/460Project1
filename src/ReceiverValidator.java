import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import util.Utility;

public class ReceiverValidator {
    static int previousAck = 0;
    private static final short GOOD_CHECKSUM = 0;

    private static ReceiverValidator validator = new ReceiverValidator();

    private ReceiverValidator(){}

    public static ReceiverValidator getValidator(){
        return validator;
     }

    private static void makeAndSendAck(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {

        int simulateErrorRng = Utility.rngErrorGenerator();

        if (simulateErrorRng == 1) { // corrupted
            data = 1;
        } else if (simulateErrorRng == 2) { // dupe
            data = previousAck;
        } else { // data should be fine to send
            previousAck = data;
        }

        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendLen(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendSeq(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendCheckSum(DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {

        short data;

        // Send the packet data back to the client as bytes
        DatagramPacket datagramWithAck =
            new DatagramPacket(ByteBuffer.allocate(4).putShort(ReceiverValidator.GOOD_CHECKSUM).array(),
                ByteBuffer.allocate(4).putShort(ReceiverValidator.GOOD_CHECKSUM).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }
}

    

