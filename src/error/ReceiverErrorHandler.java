package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Constants.BAD_CHECKSUM;
import static util.Utility.convertPacketToDatagram;

public class ReceiverErrorHandler {

    public static void sendBadChecksumToSender(DatagramSocket serverSocket, DatagramPacket receivedDatagram)
        throws IOException {
        Packet packetToSender = new Packet();
        packetToSender.setCheckSum(BAD_CHECKSUM);

        DatagramPacket datagramPacket =
            convertPacketToDatagram(packetToSender, receivedDatagram.getAddress(), receivedDatagram.getPort());
        serverSocket.send(datagramPacket);
    }
}
