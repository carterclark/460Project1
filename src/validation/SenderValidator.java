package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.DUP_ACK;
import static util.Constants.ERR_ACK;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.makeStringDatagram;

public class SenderValidator {

    public static String validatePacketFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive, long endOffset,
        long previousOffset, int bytesRead, int packetCount) throws IOException, ClassNotFoundException {

        String ackToReturn = ACK_RECEIVED;

        DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
        serverSocket.receive(receivedPacket);

        Packet packet = convertByteArrayToPacket(receivedPacket.getData());

        assert packet != null;
        if (packet.getAck() == endOffset) {
            // good ack
        } else if (packet.getAck() == previousOffset) {
            System.out.println("\t\tDuplicate Ack - Received " + packet.getAck() + ", from Receiver");
            ackToReturn = DUP_ACK;
        } else if (packet.getAck() == 1) { // Corrupted Ack
            System.out.println("\t\tCorrupted Ack - Received " + packet.getAck() + ", from Receiver.");
            ackToReturn = ERR_ACK;
        }

        if (packet.getCheckSum() == 0) {
            // good checksum
        } else {
            System.out.println("bad checksum: " + packet.getCheckSum());
        }

        if (packet.getLength() == bytesRead) {
            // good length
        } else {
            System.out.println("bad length: " + packet.getLength() + " should be " + bytesRead);
        }

        if (packet.getSeqNo() == packetCount) {
            // good seq
        } else {
            System.out.println("bad seq: " + packet.getSeqNo() + " should be " + packetCount);
        }

        receivedPacket.setData(ackToReturn.getBytes());
        serverSocket.send(makeStringDatagram(ackToReturn, receivedPacket.getAddress(), receivedPacket.getPort()));

        return ackToReturn;
    }

}
    

