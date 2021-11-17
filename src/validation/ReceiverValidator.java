package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Constants.CORRUPT;
import static util.Constants.DROP;
import static util.Constants.DUPL;
import static util.Constants.DUP_ACK;
import static util.Constants.ERR;
import static util.Constants.RECEIVED;
import static util.Constants.SENDING;
import static util.Constants.SENT;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.getAckStatus;
import static util.Utility.getCorruptedData;
import static util.Utility.printReceiverInfo;
import static util.Utility.randomNumberGenerator;
public class ReceiverValidator {

    public static void makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        Packet packetFromSender, double percentOfDataToCorrupt, long startTime, int seqNo) throws IOException {

        String packetStatus = SENT;

        Packet packetToSender =
            new Packet(packetFromSender.getCheckSum(), packetFromSender.getLength(), packetFromSender.getAck(),
                packetFromSender.getSeqNo(), new byte[1]);

        // simulate sequence error
        // randomNumberGenerator gives an integer between 1-100
        if (percentOfDataToCorrupt > 0) {
            if (randomNumberGenerator() < 10) {
                packetToSender.setSeqNo(packetFromSender.getSeqNo() - 1);
                packetStatus = DROP;
                percentOfDataToCorrupt = 0;
            } else if (randomNumberGenerator() < 10) {
                packetToSender.setSeqNo(randomNumberGenerator());
                packetStatus = DROP;
                percentOfDataToCorrupt = 0;
            }
        }
        byte[] dataToSender = convertPacketToByteArray(packetToSender);

        // simulate corruption error
        if (percentOfDataToCorrupt > 0 && randomNumberGenerator() < 10) {
            dataToSender = getCorruptedData(convertPacketToByteArray(packetToSender), percentOfDataToCorrupt);
            packetStatus = CORRUPT;
        }

        DatagramPacket datagramPacket =
            new DatagramPacket(dataToSender, dataToSender.length, receivedDatagram.getAddress(),
                receivedDatagram.getPort());

        serverSocket.send(datagramPacket);
        serverSocket.receive(receivedDatagram);

        String ackFromSender = getAckStatus(new String(receivedDatagram.getData()));
        ackFromSender = getAckStatus(ackFromSender);
        String receiverStatus = RECEIVED;

        if (ackFromSender.equals(DUP_ACK)) {
            receiverStatus = DUPL;
        }

        // Print Info
        printReceiverInfo(receiverStatus, startTime, packetFromSender.getSeqNo(), packetStatus);
        System.out.printf("\t%s ACK %s %s", SENDING, seqNo, packetStatus);
    }

}

    

