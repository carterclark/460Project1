
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver {// Server

    private static byte[] dataToReceive = new byte[4096];
    private static final short GOOD_CHECKSUM = 0;
    private static final short BAD_CHECKSUM = 1;

    public static void main(String[] args) {
        // Steps to use:
        // (first time) javac Receiver.java
        // java Receiver 8080 new_image.png

        FileOutputStream outputStream = null;
        // logging counters/variables
        int packetCount = 0;
        long startOffset = 0;
        long endOffset = 0;

        if (args.length < 2) {
            System.out.println(
                    "\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
            System.exit(1);
        }
        try {
            // initialize socket and create output stream
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(args[0]));

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                DatagramPacket receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram to hold incoming packet
                serverSocket.receive(receivedDatagram); // wait for a start packet

                // endOffset accumulates with length of data in packet, offsets are
                // relative to the file not the buffer
                endOffset += receivedDatagram.getLength();
                System.out.println("length: " + receivedDatagram.getLength()
                        + "\nendOffset" + endOffset);

                // Make packet
                makeAndSendPacket((int) endOffset, serverSocket, receivedDatagram);

                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else {
                    // if output stream is not initialized do it now
                    if (outputStream == null) {
                        outputStream = new FileOutputStream(args[1]);
                    }
                    System.out.format("Packet: %4d  -  Start Byte Offset: %8d  -  End Byte Offset: %8d%n",
                            ++packetCount, startOffset, endOffset); // progress logging
                    outputStream.write(receivedDatagram.getData(), 0, receivedDatagram.getLength());
                    startOffset = endOffset; // start offset of next packet will be end offset of current packet,
                    // offsets are relative to the file not the buffer
                }

                dataToReceive = new byte[4096]; // flush buffer
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeAndSendPacket(int endOffset, DatagramSocket serverSocket, DatagramPacket receivedDatagram) throws IOException {
        Packet packetToSend = new Packet(
                GOOD_CHECKSUM,
                (short) receivedDatagram.getLength(),
                receivedDatagram.getData()[receivedDatagram.getLength() - 1],
                endOffset,
                receivedDatagram.getData());
        System.out.println("ack to be sent: " + packetToSend.getAck());

        byte[] packetAsBytes = convertPacketToByteArray(packetToSend);

        // Send the packet data back to the client as the ack
        DatagramPacket datagramWithAck = new DatagramPacket(
                packetAsBytes,
                packetAsBytes.length,
                receivedDatagram.getAddress(),
                receivedDatagram.getPort());
        serverSocket.send(datagramWithAck);
    }

    private static byte[] convertPacketToByteArray(Packet packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(packet);
        return byteArrayOutputStream.toByteArray();
    }
}
