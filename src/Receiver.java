import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import objects.Packet;
import util.Utility;

import static util.Utility.GOOD_CHECKSUM;
import static util.Utility.MAX_PACKET_SIZE;
import static util.Utility.RECV;
import static util.Utility.convertByteArrayToPacket;
import static util.Utility.convertPacketToDatagram;

public class Receiver {// Server

    private static byte[] dataToReceive;
    protected static final int NUM_OF_FRAMES = 16;

    private static long startTime;
    private static DatagramSocket serverSocket;
    private static FileOutputStream outputStream = null;
    private static DatagramPacket receivedDatagram;
    private static int previousOffset = 0;

    public static void main(String[] args) throws SocketException, FileNotFoundException {

        // logging counters/variables
        int packetCount = 0;
        int endOffset = 0;
        ArrayList<byte[]> byteArrayList = new ArrayList<>();

        parseCommandLine(args, true);

        try {
            dataToReceive = new byte[MAX_PACKET_SIZE];

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                startTime = System.currentTimeMillis();

                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                serverSocket.receive(receivedDatagram); // wait for a start packet
                byteArrayList.add(receivedDatagram.getData());

                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    byteArrayList.remove(byteArrayList.size() - 1);
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else if (new String(receivedDatagram.getData()).trim().equals("error")) {
                    byteArrayList.remove(byteArrayList.size() - 1);
                    System.out.println("in error if statement");
                } else {
                    Packet packetFromSender = convertByteArrayToPacket(receivedDatagram.getData());
                    assert packetFromSender != null;
                    endOffset += packetFromSender.getLength();
                    packetCount = packetFromSender.getSeqNo();

                    System.out.printf(
                        "Packet: %d/%d\tStart Byte Offset:%d\tEnd Byte Offset: %d\tSent time:%d\t" + RECV + "\n",
                        packetCount, NUM_OF_FRAMES, previousOffset, endOffset,
                        (System.currentTimeMillis() - startTime));

                    makeAndSendAcknowledgement(serverSocket, receivedDatagram, packetFromSender, packetCount);

                    previousOffset = endOffset;
                }

                dataToReceive = new byte[receivedDatagram.getLength()]; // flush buffer
            }

            for (byte[] byteArray : byteArrayList) {
                Packet tempPacket = convertByteArrayToPacket(byteArray);
                assert tempPacket != null;
                assert outputStream != null;
                outputStream.write(tempPacket.getData(), 0, tempPacket.getData().length);
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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

    private static boolean errorInData() {
        return (new String(receivedDatagram.getData()).trim().equals("error"));
    }

    private static void errorFromSender() {
        // todo make method to send acknowledgement back to sender
        // todo re-receive packet and confirm it's not error signal, if it is, then kill program
        // Kenny
    }

    private static long ackErrorSim(long ack) throws IOException {

        int simulateErrorRng = Utility.rngErrorGenerator();

        if (simulateErrorRng == 1) { // corrupted
        	ack = 1;
        } else if (simulateErrorRng == 2) { // dupe
        	ack = previousOffset;
        }

        // return ack;
        return 10;
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
