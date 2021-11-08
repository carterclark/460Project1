package validation;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import objects.Packet;

import static util.Utility.convertByteArrayToPacket;

public class SenderValidator {
    private long startOffset;
    private int packetCount;
    long previousStartOffset = 0;
    private static int numOfFrames = 15;
    private long startTime;

    private static SenderValidator validator = new SenderValidator();

    private SenderValidator() {
    }

    public static SenderValidator getValidator() {
        return validator;
    }

    public static void validatePacketFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive, long startOffset,
        long previousOffset, int bytesRead, int packetCount) throws IOException, ClassNotFoundException {

        DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
        serverSocket.receive(receivedPacket);

        Packet packet = convertByteArrayToPacket(receivedPacket.getData());

        assert packet != null;
        if (packet.getAck() == startOffset) {
            // good ack
        } else if(packet.getAck() == previousOffset){
            System.out.println("\t\tDuplicate Ack - Received " + packet.getAck() + ", from Receiver");
            //todo activate error handler
        }
        else if (packet.getAck() == 1) { // Corrupted Ack
            System.out.println("\t\tCorrupted Ack - Received " + packet.getAck() + ", from Receiver.");
            // todo activate error handler
            // Tyler
        }

        if (packet.getCheckSum() == 0) {
            // good checksum
        } else {
            System.out.println("bad checksum:");
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
    }

    public DatagramPacket makeStringDatagram(String stringToSend, DatagramPacket receivedPacket) {
        byte[] data = stringToSend.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort());
    }
}
    

