import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

import objects.Packet;

import static error.ReceiverErrorHandler.sendBadChecksumToSender;
import static util.Constants.DUPL;
import static util.Constants.DUP_ACK;
import static util.Constants.ERR;
import static util.Constants.ERR_ACK;
import static util.Constants.MAX_PACKET_SIZE;
import static util.Constants.OUT_OF_SEQUENCE;
import static util.Constants.RECEIVED;
import static util.Constants.SENDING;
import static util.Constants.SENT;
import static util.Constants.TIMEOUT;
import static util.Constants.TIMEOUT_MAX;
import static util.Utility.Usage;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.makeSpaces;
import static util.Utility.printReceiverInfo;
import static validation.ReceiverValidator.makeAndSendAcknowledgement;

public class Receiver {// Server

    private static DatagramSocket socketToSender;
    private static FileOutputStream outputStream = null;
    private static double percentOfDataToCorrupt = 0.25;

    public static void main(String[] args) throws SocketException, FileNotFoundException, ClassNotFoundException {

        // logging counters/variables
        int previousPacketCount = 1;
        ArrayList<Packet> packetList = new ArrayList<>();
        parseCommandLine(args, false);
        socketToSender.setSoTimeout((int) TIMEOUT_MAX);

        try {
            System.out.println("\nStarting Receiver\n");
            while (true) {
                byte[] dataToReceive = new byte[MAX_PACKET_SIZE];
                long startTime = System.currentTimeMillis();

                DatagramPacket receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length);
                socketToSender.receive(receivedDatagram); // wait for a start packet

                Packet packetFromSender;

                if (new String(receivedDatagram.getData()).startsWith("error")) { // received error ack
                    if (!packetList.isEmpty()) {
                        packetList.remove(packetList.size() - 1);
                    }
                } else if (new String(receivedDatagram.getData()).startsWith("stop")) {
                    System.out.println("\n\t\tPacket retry failed, stopping program");
                    System.exit(400);
                } else {
                    try {
                        // receivedDatagram has bytes that need to be translated to a Packet object
                        packetFromSender = convertByteArrayToPacket(receivedDatagram.getData());
                    } catch (StreamCorruptedException e) {
                        // if the receivedDatagram is corrupted, an exception would be thrown
                        String ackFromSender = sendBadChecksumToSender(socketToSender, receivedDatagram);
                        printReceiverInfo(RECEIVED, startTime, previousPacketCount, ackFromSender);
                        // Print Ack
                        System.out.printf("\t%s\tACK %s %s", SENDING, previousPacketCount + 1, SENT);
                        continue;
                    }
                    assert packetFromSender != null;

                    // check if sender is done
                    if (Arrays.equals(packetFromSender.getData(), new byte[0])) {
                        if (!packetList.isEmpty()) {
                            packetList.remove(packetList.size() - 1);
                            // get rid of last empty packet
                            // because last packet sent has empty byte array we don't want to read
                        }
                        System.out.println("\nReceived end packet.  Terminating.");
                        break;
                    }
                    makeAndSendAcknowledgement(socketToSender, receivedDatagram, packetFromSender,
                        percentOfDataToCorrupt, startTime, packetFromSender.getSeqNo());

                    packetList.add(packetFromSender);
                    previousPacketCount = packetFromSender.getSeqNo();
                }
            }

            // We are not writing the data in the while loop because
            // we might need to get rid of certain packets with errors.
            // Here we create the image with bytes stored in the Packets
            for (Packet packet : packetList) {
                assert packet != null;
                assert outputStream != null;
                // this creates the new_image.png
                outputStream.write(packet.getData(), 0, packet.getData().length);
            }

            // done, close sockets/streams
            socketToSender.close();
            outputStream.close();

        } catch (SocketTimeoutException ex) {
            System.out.println(TIMEOUT + " On Sequence " + previousPacketCount + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void parseCommandLine(String[] args, boolean overrideParse)
        throws FileNotFoundException, SocketException {
        int index = 0;
        String arg;

        if (overrideParse) {
            socketToSender = new DatagramSocket(8080);
            outputStream = new FileOutputStream("src/new_image.png");
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
                        socketToSender = new DatagramSocket(Integer.parseInt(args[index]));
                    }

                    if (index == (args.length - 1)) {
                        outputStream = new FileOutputStream(args[index]);
                    }
                }
                index++;
            }

        }

    }

}
