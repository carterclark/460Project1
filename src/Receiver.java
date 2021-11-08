import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import objects.Packet;
import util.Utility;

import static util.Utility.RECV;
import static util.Utility.convertPacketToDatagram;

public class Receiver {// Server

    private static byte[] dataToReceive;
    private static final short GOOD_CHECKSUM = 0;
    private static final short BAD_CHECKSUM = 1;
    protected static final int NUM_OF_FRAMES = 16;


    private static long startTime;
    private static DatagramSocket serverSocket;
    private static DatagramPacket receivedDatagram;
    static int previousAck = 0;

    public static void main(String[] args) {
        // Steps to use:
        // javac Receiver.java
        // java Receiver 8080 new_image.png

        FileOutputStream outputStream = null;
        // logging counters/variables
        int packetCount = 1;
        long startOffset = 0;
        long endOffset = 0;

        if (args.length < 2) {
            System.out.println(
                "\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
            System.exit(1);
        }
        try {
            // initialize socket and create output stream
            serverSocket = new DatagramSocket(Integer.parseInt(args[0]));
            dataToReceive = new byte[700];

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                startTime = System.currentTimeMillis();

                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                serverSocket.receive(receivedDatagram); // wait for a start packet

                // endOffset accumulates with length of data in packet, offsets are
                // relative to the file not the buffer
                endOffset += receivedDatagram.getLength();

                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else {
                    // if output stream is not initialized do it now
                    if (outputStream == null) {
                        outputStream = new FileOutputStream(args[1]);
                    }

                    System.out.printf(
                        "Packet: %d/%d - Start Byte Offset:%d" + " - End Byte Offset: %d - Sent time:%d - " + RECV +
                            "\n",
                        packetCount, NUM_OF_FRAMES, startOffset, endOffset, (System.currentTimeMillis() - startTime));

                    makeAndSendAcknowledgement(serverSocket, receivedDatagram, (int) endOffset,
                        packetCount++);


                    outputStream.write(receivedDatagram.getData(), 0, receivedDatagram.getLength());
                    startOffset = endOffset; // start offset of next packet will be end offset of current packet,
                    // offsets are relative to the file not the buffer
                }

                dataToReceive = new byte[receivedDatagram.getLength()]; // flush buffer
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();
        } catch (IOException e) {
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

    private static void makeAndSendAck(int data, DatagramSocket serverSocket, DatagramPacket datagramPacket)
        throws IOException {

        int simulateErrorRng = Utility.rngErrorGenerator();

        if (simulateErrorRng == 1) { // corrupted
            data = 1;
        } else if (simulateErrorRng == 2) { // dupe
            data = previousAck;
        } else { // data should be fine to send
            previousAck = data;
        }

        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, datagramPacket.getAddress(), datagramPacket.getPort());
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendLen(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendSeq(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendCheckSum(DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException {

        // Send the packet data back to the client as bytes
        DatagramPacket datagramWithAck =
            new DatagramPacket(ByteBuffer.allocate(4).putShort(Receiver.GOOD_CHECKSUM).array(),
                ByteBuffer.allocate(4).putShort(Receiver.GOOD_CHECKSUM).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendAcknowledgement(DatagramSocket serverSocket, DatagramPacket receivedDatagram,
        int endOffset, int packetCount) throws IOException {

        Packet packet = new Packet(GOOD_CHECKSUM, (short) receivedDatagram.getLength(), endOffset, packetCount,
            new byte[1]);

        DatagramPacket datagramPacket =
            convertPacketToDatagram(packet, receivedDatagram.getAddress(), receivedDatagram.getPort());
        serverSocket.send(datagramPacket);
    }
}
