package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class ReceiverHandler
{
    private static DatagramSocket serverSocket;

    private static DatagramPacket receivedDatagram;

    private static byte[] dataToReceive = new byte[4096];

    private static int PORT = 8081;

    public static void main(String[] args) throws IOException
    {
        receivePacket();
    }

    public static void receivePacket() throws IOException
    {
        serverSocket = new DatagramSocket(PORT);
        receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length);
        System.out.println("Waiting for data from error sender------\n");
        serverSocket.receive(receivedDatagram);

        int dataFromReceiver = ByteBuffer.wrap(receivedDatagram.getData()).getInt();
        System.out.println("number from receiver: " + dataFromReceiver);
    }
}
