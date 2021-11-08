import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

import javax.xml.crypto.Data;

public class SenderValidator {
    private long startOffset;
    private int packetCount;
    long previousStartOffset = 0;
    private static int numOfFrames = 15;
    private long startTime;

    private static SenderValidator validator = new SenderValidator();

    private SenderValidator(){}

    public static SenderValidator getValidator(){
        return validator;
     }

    public void validateAckFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive) throws IOException {

        while (true) {
            // Receive the server's packet
            DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
            serverSocket.receive(receivedPacket);

            int ackFromReceiver = ByteBuffer.wrap(receivedPacket.getData()).getInt();

            // Check ack from server
            if (ackFromReceiver == startOffset) { // Good ack
                previousStartOffset = startOffset;
                break;
            } else if (ackFromReceiver == previousStartOffset) { // Dupe Ack
                System.out.println("\t\tDuplicate Ack - Received " + ackFromReceiver + ", from Receiver");
                serverSocket.send(makeStringDatagram("error", receivedPacket));
                validateAckFromReceiver(serverSocket, dataToReceive);
                break;
            } else if (ackFromReceiver == 1) { // Corrupted Ack
                System.out.println("\t\tCorrupted Ack - Received " + ackFromReceiver + ", from Receiver.");
                //todo send error signal to receiver
                System.exit(500); //todo here: call validateAckFromReceiver method again
                // Tyler
            }
        }
    }

    public void validateCheckSumFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive) throws IOException {
        while (true) {
            // Receive the server's packet
            DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
            serverSocket.receive(receivedPacket);

            short checkSum = ByteBuffer.wrap(receivedPacket.getData()).getShort();

            // check for 0 from server
            if (checkSum == 0) {
                break;
            }
            System.out.println("received " + checkSum + " as a checksum");
        }
    }
    public void validateLenFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive, int senderLen)
            throws IOException {
        while (true) {
            // Receive the server's packet
            DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
            serverSocket.receive(receivedPacket);

            int lenFromReceiver = ByteBuffer.wrap(receivedPacket.getData()).getInt();

            // Check len from server
            if (lenFromReceiver == senderLen) {
                break;
            }
            System.out.println("received " + senderLen + " as length");
        }
    }

    public void validateSequenceFromReceiver(DatagramSocket serverSocket, byte[] dataToReceive, int senderSequence)
            throws IOException {
        while (true) {
            // Receive the server's packet
            DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length);
            serverSocket.receive(receivedPacket);

            int receiverSequence = ByteBuffer.wrap(receivedPacket.getData()).getInt();
            // Check ack from server
            if (receiverSequence == senderSequence) {
                break;
            }
            System.out.println("Error: received " + receiverSequence + " as sequence number");
        }
    }

    public DatagramPacket makeStringDatagram(String stringToSend, DatagramPacket receivedPacket){
        byte[] data = stringToSend.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort());
    }
}
    

