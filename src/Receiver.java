import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import objects.Packet;

import static error.ReceiverErrorHandler.sendBadChecksumToSender;
import static util.Constants.MAX_PACKET_SIZE;
import static util.Constants.RECEIVING;
import static util.Constants.TIMEOUT_MAX;
import static util.Utility.Usage;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.makeSpaces;
import static validation.ReceiverValidator.makeAndSendAcknowledgement;

public class Receiver {// Server

    private static byte[] dataToReceive;

    private static long startTime;
    private static DatagramSocket socketToSender;
    private static FileOutputStream outputStream = null;
    private static DatagramPacket receivedDatagram;
    private static int previousOffset = 0;
    private static double percentOfDataToCorrupt = 0;

    public static void main(String[] args) throws SocketException, FileNotFoundException, ClassNotFoundException {

        // logging counters/variables
        int endOffset;
        int packetCount = 1;
        ArrayList<Packet> packetList = new ArrayList<>();
        parseCommandLine(args, true);
        socketToSender.setSoTimeout((int) TIMEOUT_MAX);

        try {
            System.out.println("\nStarting Receiver\n");
            while (true) {
                dataToReceive = new byte[MAX_PACKET_SIZE];
                startTime = System.currentTimeMillis();

                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                socketToSender.receive(receivedDatagram); // wait for a start packet

                Packet packetFromSender;

                if (new String(receivedDatagram.getData()).startsWith("error")) { // received error ack
                    if (!packetList.isEmpty()) {
                        packetList.remove(packetList.size() - 1);
                    }
                } else if (new String(receivedDatagram.getData()).startsWith("stop")) {
                    System.out.println("\t\tPacket retry failed, stopping program");
                    System.exit(400);
                } else {
                    try {
                        packetFromSender = convertByteArrayToPacket(receivedDatagram.getData());
                    } catch (StreamCorruptedException e) {
                        String status = sendBadChecksumToSender(socketToSender, receivedDatagram);
                        printReceiverInfo(RECEIVING, startTime, packetCount, status);
                        continue;
                    }
                    assert packetFromSender != null;

                    // check if sender is done
                    if (Arrays.equals(packetFromSender.getData(), new byte[0])) {
                        if (!packetList.isEmpty()) {
                            packetList.remove(packetList.size() - 1);
                            // get rid of last empty packet
                        }
                        System.out.println("Received end packet.  Terminating.");
                        break;
                    }
                    endOffset = (int) packetFromSender.getAck();
                    String packetStatus = makeAndSendAcknowledgement(socketToSender, receivedDatagram, packetFromSender,
                        percentOfDataToCorrupt);

                    printReceiverInfo(RECEIVING, startTime, packetFromSender.getSeqNo(), packetStatus);

                    packetList.add(packetFromSender);
                    packetCount = packetFromSender.getSeqNo();
                    previousOffset = endOffset;
                }
            }

            for (Packet packet : packetList) {
                assert packet != null;
                assert outputStream != null;
                outputStream.write(packet.getData(), 0, packet.getData().length);
            }

            // done, close sockets/streams
            socketToSender.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printReceiverInfo(String receiverAction, long startTime, int packetCount,
        String receiverCondition) {

        System.out.printf("%s:\t%s%s%s\n", receiverAction, makeSpaces(System.currentTimeMillis() - startTime),
            makeSpaces(packetCount), receiverCondition);
    }

    private static void parseCommandLine(String[] args, boolean overrideParse)
        throws FileNotFoundException, SocketException {
        int index = 0;
        String arg;

        if (overrideParse) {
            socketToSender = new DatagramSocket(8080);
            outputStream = new FileOutputStream("new_image.png");
        } else {
            if (args.length < 2) {
                System.out.println(
                    "\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
                System.exit(1);
            }
            while (index < args.length) {
                arg = args[index];

                // process any command line switches
                if (arg.startsWith("-")) {
                    if (arg.charAt(1) == 'd') {
                        if (args[index + 1].startsWith("-")) {
                            System.err.println("-d requires a percent (as decimal) to corrupt");
                            Usage();
                        } else {
                            percentOfDataToCorrupt = Double.parseDouble(args[++index]);
                        }
                    }
                } else {
                    if (index == (args.length - 2)) {
                        outputStream = new FileOutputStream(args[index]);
                    }

                    if (index == (args.length - 1)) {
                        socketToSender = new DatagramSocket(Integer.parseInt(args[index]));
                    }
                }
                index++;
            }

        }

    }

}
