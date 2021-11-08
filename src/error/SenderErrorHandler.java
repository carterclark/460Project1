package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import objects.Packet;
import util.Utility;

import static util.Utility.convertPacketToDatagram;
import static util.Utility.makeGenericPacket;

public class SenderErrorHandler {

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

    public SenderErrorHandler() throws UnknownHostException {
    }

    public static void main(String[] args) throws IOException {
        //        int testNum = 1;
        //        System.out.println("testNum: " + testNum);
        //        Utility.changeNumber(testNum);
        //        System.out.println("testNum: " + testNum);
        serverSocket = new DatagramSocket();
        sendPacket();
    }

    public static void sendPacket() throws IOException {
        //        int numToSend = 123;
        //        datagramPacket = new DatagramPacket(ByteBuffer.allocate(4).putInt(numToSend).array(),
        //            ByteBuffer.allocate(4).putInt(numToSend).array().length, INET_ADDRESS, PORT);

        System.out.println("Sending Packet-----------");
        datagramPacket = convertPacketToDatagram(makeGenericPacket(), INET_ADDRESS, PORT);
        serverSocket.send(datagramPacket);
    }

    // Carter todo figure out how to send a packet over socket
    //todo make class for sender validations and migrate all methods
    //todo make class for receiver validations and migrate all methods
    //todo migrate other sender base methods to utility class
}
