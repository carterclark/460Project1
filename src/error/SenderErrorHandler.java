package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class SenderErrorHandler
{

    private static DatagramSocket serverSocket;

    private static DatagramPacket datagramPacket;

    private static byte[] data = new byte[4096];

    private static int PORT = 8081;

    private static InetAddress INET_ADDRESS;

    static {
        try {
            INET_ADDRESS = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public SenderErrorHandler() throws UnknownHostException
    {
    }

    public static void main(String[] args) throws IOException
    {
        sendPacket();
    }

    public static void sendPacket() throws IOException
    {
        serverSocket = new DatagramSocket();
        datagramPacket = new DatagramPacket(ByteBuffer.allocate(4).putInt(123).array(),
            ByteBuffer.allocate(4).putInt(123).array().length, INET_ADDRESS, PORT);

        serverSocket.send(datagramPacket);
    }
}
