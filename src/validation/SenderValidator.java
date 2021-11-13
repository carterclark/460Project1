package validation;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.BAD_CHECKSUM;
import static util.Constants.CORRUPT;
import static util.Constants.DUP_ACK;
import static util.Constants.OUT_OF_SEQUENCE;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.makeStringDatagram;

public class SenderValidator {

    public static String validatePacketFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive, long endOffset,
        long previousOffset, int bytesRead, int packetCount) throws IOException, ClassNotFoundException {

        String ackToReturn = ACK_RECEIVED;

        DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
        serverSocket.receive(receivedPacket);
        try {
            Packet packet = convertByteArrayToPacket(receivedPacket.getData());
            assert packet != null;

            if (packet.getCheckSum() == BAD_CHECKSUM || packet.getAck() != endOffset
                || packet.getLength() != bytesRead) {
                ackToReturn = CORRUPT;
            } else {
                if (packet.getAck() == previousOffset) {
                    ackToReturn = DUP_ACK;
                } else if (packet.getSeqNo() == packetCount - 1) {
                    ackToReturn = DUP_ACK;
                } else if (packet.getSeqNo() != packetCount) {
                    ackToReturn = OUT_OF_SEQUENCE;
                }
            }
        } catch (StreamCorruptedException e) {
            ackToReturn = CORRUPT;
        }
        serverSocket.send(makeStringDatagram(ackToReturn, receivedPacket.getAddress(), receivedPacket.getPort()));

        return ackToReturn;
    }

}
    

