import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

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
    private static DatagramPacket receivedDatagram;
    private static int previousOffset = 0;

    public static void main(String[] args) {
        // Steps to use:
        // javac Receiver.java
        // java Receiver 8080 new_image.png

        FileOutputStream outputStream = null;
        // logging counters/variables
        int packetCount = 1;
        int endOffset = 0;

        if (args.length < 2) {
            System.out.println(
                "\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
            System.exit(1);
        }
        try {
            // initialize socket and create output stream
            serverSocket = new DatagramSocket(Integer.parseInt(args[0]));
            dataToReceive = new byte[MAX_PACKET_SIZE];

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                startTime = System.currentTimeMillis();

                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                serverSocket.receive(receivedDatagram); // wait for a start packet

//                DatagramPacket tempDatagram = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);
//                serverSocket.receive(tempDatagram);


                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else {
                    Packet packetFromSender = convertByteArrayToPacket(receivedDatagram.getData());
                    assert packetFromSender != null;
                    endOffset += packetFromSender.getLength();

                    // if output stream is not initialized do it now
                    if (outputStream == null) {
                        outputStream = new FileOutputStream(args[1]);
                    }

                    System.out.printf(
                        "Packet: %d/%d - Start Byte Offset:%d" + " - End Byte Offset: %d - Sent time:%d - " + RECV +
                            "\n",
                        packetCount, NUM_OF_FRAMES, previousOffset, endOffset, (System.currentTimeMillis() - startTime));

                    makeAndSendAcknowledgement(serverSocket, receivedDatagram, packetFromSender,
                        packetCount++);


                    previousOffset = endOffset;
                    outputStream.write(packetFromSender.getData(), 0, packetFromSender.getLength());
                    // offsets are relative to the file not the buffer
                }

                dataToReceive = new byte[receivedDatagram.getLength()]; // flush buffer
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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

    private static long ackErrorSim(long ack)
        throws IOException {

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
