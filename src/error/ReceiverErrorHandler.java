package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Utility.convertByteArrayToPacket;

public class ReceiverErrorHandler {
    private static DatagramSocket serverSocket;
    private static DatagramPacket receivedDatagram;
    private static byte[] dataToReceive = new byte[4096];
    private static int PORT = 8081;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        receivePacket();
    }

    public static void receivePacket() throws IOException, ClassNotFoundException {
        serverSocket = new DatagramSocket(PORT);
        receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length);
        System.out.println("Waiting for data from error sender------\n");
        serverSocket.receive(receivedDatagram);

        Packet packet = convertByteArrayToPacket(receivedDatagram.getData());
        assert packet != null;
        System.out.println("Expecting 'Success' :" + new String(packet.getData()));
    }
}
