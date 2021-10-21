
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Receiver {// Server

    private static DatagramSocket serverSocket;
    private static byte[] dataToReceive = new byte[4096];
    private static byte[] dataToSend = new byte[4096];

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
            serverSocket = new DatagramSocket(Integer.parseInt(args[0]));

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                DatagramPacket receivedPacket = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram to hold incoming packet
                serverSocket.receive(receivedPacket); // wait for a start packet

                // Get the message from the packet
                byte[] message = "ok".getBytes(StandardCharsets.UTF_8);

                // Send the packet data back to the client
                DatagramPacket packetToSend = new DatagramPacket(
                        message,
                        message.length,
                        receivedPacket.getAddress(),
                        receivedPacket.getPort());
                serverSocket.send(packetToSend);

                endOffset += receivedPacket.getLength(); // endOffset accumulates with length of data in packet, offsets are
                // relative to the file not the buffer
                if (new String(receivedPacket.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                } else {
                    // if output stream is not initialized do it now
                    if (outputStream == null) {
                        outputStream = new FileOutputStream(args[1]);
                    }
                    System.out.format("Packet: %4d  -  Start Byte Offset: %8d  -  End Byte Offset: %8d%n",
                            ++packetCount, startOffset, endOffset); // progress logging
                    outputStream.write(receivedPacket.getData(), 0, receivedPacket.getLength());
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
}
