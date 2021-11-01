
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Receiver {// Server

    private static final int MAX_PACKET_SIZE = 4096; // default buffer will send the data in 4K chunks

    private static byte[] dataToReceive = new byte[MAX_PACKET_SIZE];
    private static final short GOOD_CHECKSUM = 0;
    private static final short BAD_CHECKSUM = 1;

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
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(args[0]));

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                DatagramPacket receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram to hold incoming packet
                serverSocket.receive(receivedDatagram); // wait for a start packet

                // endOffset accumulates with length of data in packet, offsets are
                // relative to the file not the buffer
                endOffset += receivedDatagram.getLength();

                // send ack
                makeAndSendInt((int) endOffset, serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());

                //send checksum
                makeAndSendShort(GOOD_CHECKSUM, serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());

                //send len
                makeAndSendInt(receivedDatagram.getLength(), serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());

                //send sequence number
                makeAndSendInt(packetCount, serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());

                //create data with packet
//                byte[] packetData = Utility.convertPacketToByteArray(new Packet(GOOD_CHECKSUM, (short) receivedDatagram.getLength(), (int) endOffset, packetCount, receivedDatagram.getData()));
//                //send packet
//                DatagramPacket datagramWithPacket = new DatagramPacket(
//                        packetData,
//                        packetData.length,
//                        receivedDatagram.getAddress(),
//                        receivedDatagram.getPort()
//                );
//                serverSocket.send(datagramWithPacket);

                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else {
                    // if output stream is not initialized do it now
                    if (outputStream == null) {
                        outputStream = new FileOutputStream(args[1]);
                    }
                    System.out.format("Packet: %4d  -  Start Byte Offset: %8d  -  End Byte Offset: %8d%n",
                            packetCount++, startOffset, endOffset); // progress logging
                    outputStream.write(receivedDatagram.getData(), 0, receivedDatagram.getLength());
                    startOffset = endOffset; // start offset of next packet will be end offset of current packet,
                    // offsets are relative to the file not the buffer
                }

                dataToReceive = new byte[MAX_PACKET_SIZE]; // flush buffer
            }

            // done, close sockets/streams
            serverSocket.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeAndSendInt(int data,
                                       DatagramSocket serverSocket,
                                       InetAddress inetAddress,
                                       int port) throws IOException {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(
                ByteBuffer.allocate(4).putInt(data).array(),
                ByteBuffer.allocate(4).putInt(data).array().length,
                inetAddress,
                port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendShort(short data,
                                         DatagramSocket serverSocket,
                                         InetAddress inetAddress,
                                         int port) throws IOException {

        // Send the packet data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(
                ByteBuffer.allocate(4).putShort(data).array(),
                ByteBuffer.allocate(4).putShort(data).array().length,
                inetAddress,
                port);
        serverSocket.send(datagramWithAck);
    }
}
