import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import objects.Packet;
import util.Utility;

import static util.Constants.GOOD_CHECKSUM;
import static util.Constants.MAX_PACKET_SIZE;
import static util.Constants.RECEIVED;
import static util.Constants.RECEIVING;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.convertPacketToDatagram;
import static util.Utility.makeSpaces;

public class Receiver {// Server

    private static byte[] dataToReceive;

    private static long startTime;
    private static DatagramSocket serverSocket;
    private static FileOutputStream outputStream = null;
    private static DatagramPacket receivedDatagram;
    private static int previousOffset = 0;

    public static void main(String[] args) throws SocketException, FileNotFoundException {

        // logging counters/variables
        int endOffset = 0;
        ArrayList<Packet> packetList = new ArrayList<>();
        parseCommandLine(args, true);

        try {
            System.out.println("\nStarting Receiver\n");
            while (true) {
                dataToReceive = new byte[MAX_PACKET_SIZE];
                startTime = System.currentTimeMillis();

                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                serverSocket.receive(receivedDatagram); // wait for a start packet

                if (new String(receivedDatagram.getData()).startsWith("end")) {
                    if (!packetList.isEmpty()) {
                        packetList.remove(packetList.size() - 1);
                    }
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else if (new String(receivedDatagram.getData()).startsWith("error")) {
                    if (!packetList.isEmpty()) {
                        packetList.remove(packetList.size() - 1);
                    }
                } else if (new String(receivedDatagram.getData()).startsWith("stop")) {
                    System.out.println("\t\tPacket retry failed, stopping program");
                    System.exit(400);
                } else {
                    Packet packetFromSender = convertByteArrayToPacket(receivedDatagram.getData());
                    assert packetFromSender != null;

                    endOffset = (int) packetFromSender.getAck();
                    printReceiverInfo(RECEIVING, startTime, packetFromSender.getSeqNo(), RECEIVED);
                    makeAndSendAcknowledgement(serverSocket, receivedDatagram, packetFromSender,
                        packetFromSender.getSeqNo());

                    packetList.add(packetFromSender);
                    previousOffset = endOffset;
                }
            }

            for (Packet packet : packetList) {
                assert packet != null;
                assert outputStream != null;
                outputStream.write(packet.getData(), 0, packet.getData().length);
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void printReceiverInfo(String receiverAction, long startTime, int packetCount,
        String receiverCondition) {

        System.out.printf("%s:\t%s%s%s\n", receiverAction,
            makeSpaces(System.currentTimeMillis() - startTime), makeSpaces(packetCount), receiverCondition);
    }

    private static void parseCommandLine(String[] args, boolean overrideParse)
        throws FileNotFoundException, SocketException {

        if (overrideParse) {
            serverSocket = new DatagramSocket(8080);
            outputStream = new FileOutputStream("new_image.png");
        } else {
            if (args.length < 2) {
                System.out.println(
                    "\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
                System.exit(1);
            } else {
                serverSocket = new DatagramSocket(Integer.parseInt(args[0]));
                outputStream = new FileOutputStream(args[1]);
            }
        }

    }

    private static long ackErrorSim(long ack) throws IOException {

        int simulateErrorRng = Utility.rngErrorGenerator();

        if (simulateErrorRng == 1) { // corrupted
            ack = 1;
        } else if (simulateErrorRng == 2) { // dupe
            ack = previousOffset;
        }

        return ack;
    }

    private static void makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        Packet packetFromSender, int packetCount) throws IOException {

        packetFromSender.setAck(ackErrorSim(packetFromSender.getAck()));

        Packet packet = new Packet(GOOD_CHECKSUM, packetFromSender.getLength(), packetFromSender.getAck(), packetCount,
            new byte[1]);

        DatagramPacket datagramPacket =
            convertPacketToDatagram(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
        serverSocket.send(datagramPacket);
    }
}
