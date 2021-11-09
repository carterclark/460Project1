package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import error.SenderErrorHandler;
import objects.Packet;

import static util.Utility.convertByteArrayToPacket;

public class SenderValidator {

    private static SenderValidator validator = new SenderValidator();

    private SenderValidator() {
    }

    public static SenderValidator getValidator() {
        return validator;
    }

    public static boolean validatePacketFromReceiver(DatagramSocket serverSocket, DatagramPacket datagramPacketToSend,
        byte[] dataToReceive, long endOffset, long previousOffset, int bytesRead, int packetCount)
        throws IOException, ClassNotFoundException {

        DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
        serverSocket.receive(receivedPacket);

        Packet packet = convertByteArrayToPacket(receivedPacket.getData());

        assert packet != null;
        if (packet.getAck() == endOffset) {
            // good ack
        } else if (packet.getAck() == previousOffset) {
            System.out.println("\t\tDuplicate Ack - Received " + packet.getAck() + ", from Receiver");
            return false;
        } else if (packet.getAck() == 1) { // Corrupted Ack
            System.out.println("\t\tCorrupted Ack - Received " + packet.getAck() + ", from Receiver.");
            return false;
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

        return true;
    }

}
    

