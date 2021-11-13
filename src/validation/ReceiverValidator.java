package validation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Constants.BAD_CHECKSUM;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.convertPacketToDatagram;
import static util.Utility.getAckStatus;
import static util.Utility.getCorruptedData;
import static util.Utility.rngErrorGenerator;
public class ReceiverValidator {

    public static String makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        Packet packetFromSender, double percentOfDataToCorrupt) throws IOException {

        Packet packetToSender =
            new Packet(packetFromSender.getCheckSum(), packetFromSender.getLength(), packetFromSender.getAck(),
                packetFromSender.getSeqNo(), new byte[1]);

        //error sim
        byte[] corruptedData = getCorruptedData(convertPacketToByteArray(packetToSender), percentOfDataToCorrupt);
        DatagramPacket datagramPacket =
            new DatagramPacket(corruptedData, corruptedData.length, receivedDatagram.getAddress(),
                receivedDatagram.getPort());

        serverSocket.send(datagramPacket);
        serverSocket.receive(receivedDatagram);

        return getAckStatus(new String(receivedDatagram.getData()));
    }

    private static void ackErrorSim(Packet packetToSender, int previousOffset, double percentOfDataToCorrupt) {

        if (rngErrorGenerator() < 6) { // corrupted
            packetToSender.setCheckSum(BAD_CHECKSUM);
        } else if (rngErrorGenerator() < 9) { // dupe
            packetToSender.setAck(previousOffset);
        } else if (rngErrorGenerator() < 12) { // dupe
            packetToSender.setSeqNo(packetToSender.getSeqNo() - 1);
        }

    }
}

    

