package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Utility.convertPacketToByteArray;
import static util.Utility.getAckStatus;
import static util.Utility.getCorruptedData;
import static util.Utility.randomNumberGenerator;
public class ReceiverValidator {

    public static String makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        Packet packetFromSender, double percentOfDataToCorrupt) throws IOException {

        Packet packetToSender =
            new Packet(packetFromSender.getCheckSum(), packetFromSender.getLength(), packetFromSender.getAck(),
                packetFromSender.getSeqNo(), new byte[1]);

        // simulate sequence error
        // randomNumberGenerator gives an integer between 1-100
        if (randomNumberGenerator() < 15) {
            packetToSender.setSeqNo(packetFromSender.getSeqNo() - 1);
        } else if (randomNumberGenerator() < 15) {
            packetToSender.setSeqNo(randomNumberGenerator());
        }

        byte[] dataToSender = convertPacketToByteArray(packetToSender);

        // simulate corruption error
        if (percentOfDataToCorrupt > 0 && randomNumberGenerator() < 15) {
            dataToSender = getCorruptedData(convertPacketToByteArray(packetToSender), percentOfDataToCorrupt);
        }

        DatagramPacket datagramPacket =
            new DatagramPacket(dataToSender, dataToSender.length, receivedDatagram.getAddress(),
                receivedDatagram.getPort());

        serverSocket.send(datagramPacket);
        serverSocket.receive(receivedDatagram);

        return getAckStatus(new String(receivedDatagram.getData()));
    }

}

    

