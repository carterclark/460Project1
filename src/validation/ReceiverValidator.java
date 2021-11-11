package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;
import util.Utility;

import static util.Constants.GOOD_CHECKSUM;
import static util.Utility.convertPacketToDatagram;
import static util.Utility.getAckStatus;
public class ReceiverValidator {

    public static String makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        Packet packetFromSender, int previousOffset, int packetCount) throws IOException {

        packetFromSender.setAck(ackErrorSim(packetFromSender.getAck(), previousOffset));

        Packet packet = new Packet(GOOD_CHECKSUM, packetFromSender.getLength(), packetFromSender.getAck(), packetCount,
            new byte[1]);

        DatagramPacket datagramPacket =
            convertPacketToDatagram(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
        serverSocket.send(datagramPacket);

        serverSocket.receive(receivedDatagram);

        return getAckStatus(new String(receivedDatagram.getData()));
    }

    private static long ackErrorSim(long ack, int previousOffset) {

        int simulateErrorRng = Utility.rngErrorGenerator();

        if (simulateErrorRng == 1) { // corrupted
            ack = 1;
        } else if (simulateErrorRng == 2) { // dupe
            ack = previousOffset;
        }

        return ack;
    }
}

    

