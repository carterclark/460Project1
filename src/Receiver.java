import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class Receiver
{// Server

    private static final int MAX_PACKET_SIZE = 4096; // default buffer will send the data in 4K chunks
    private static byte[] dataToReceive = new byte[MAX_PACKET_SIZE];
    private static final short GOOD_CHECKSUM = 0;
    private static final short BAD_CHECKSUM = 1;
    private static DatagramSocket serverSocket;
    private static DatagramPacket receivedDatagram;

    // Mitch Testing
    static int previousAck = 0;

    public static void main(String[] args)
    {
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

            System.out.println("\nWAITING FOR FILE\n");
            while (true) {
                receivedDatagram = new DatagramPacket(dataToReceive, dataToReceive.length); // datagram
                serverSocket.receive(receivedDatagram); // wait for a start packet

                if(errorInData()){
                    errorFromSender();
                }

                // endOffset accumulates with length of data in packet, offsets are
                // relative to the file not the buffer
                endOffset += receivedDatagram.getLength();

                // Send acknowledgements
                makeAndSendAck((int) endOffset, serverSocket, receivedDatagram.getAddress(),
                    receivedDatagram.getPort());
                makeAndSendCheckSum(serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());
                makeAndSendLen(receivedDatagram.getLength(), serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());
                makeAndSendSeq(packetCount, serverSocket, receivedDatagram.getAddress(), receivedDatagram.getPort());

                if (new String(receivedDatagram.getData()).trim().equals("end")) {
                    System.out.println("Received end packet.  Terminating.");
                    break;
                }
                else {
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

    private static boolean errorInData(){
        // todo check if data in received datagram is any of the error signals
        return (new String(receivedDatagram.getData()).trim().equals("error"));
    }

    private static void errorFromSender(){
        // todo make method to send back to sender
        // todo re-receive packet and confirm it's not error signal, if it is, then kill program
        // Kenny
    }

    private static void makeAndSendAck(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {

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
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendLen(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendSeq(int data, DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {
        // Send the integer data back to the client as bytes
        DatagramPacket datagramWithAck = new DatagramPacket(ByteBuffer.allocate(4).putInt(data).array(),
            ByteBuffer.allocate(4).putInt(data).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }

    private static void makeAndSendCheckSum(DatagramSocket serverSocket, InetAddress inetAddress, int port)
        throws IOException
    {

        short data;

        // Send the packet data back to the client as bytes
        DatagramPacket datagramWithAck =
            new DatagramPacket(ByteBuffer.allocate(4).putShort(Receiver.GOOD_CHECKSUM).array(),
                ByteBuffer.allocate(4).putShort(Receiver.GOOD_CHECKSUM).array().length, inetAddress, port);
        serverSocket.send(datagramWithAck);
    }
}
