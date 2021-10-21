
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static util.Util.removeSpaces;

public class Sender extends SenderUtil {// Client

    // Steps to use:
    // (first time) javac Sender.java
    // java Sender localhost 8080 image.png 16

    // main
    public static void main(String[] args) {
        Sender sender = new Sender();
        sender.run(args);
    }

    private void run(String[] args) {
        InetAddress address;
        DatagramSocket serverSocket;

        // verify there are at least three parameters being passed in
        if (args.length < 3) {
            System.out.println("\n\nINSUFFICIENT COMMAND LINE ARGUMENTS\n\n");
            Usage();
        }
        ParseCmdLine(args); // parse the parameters that were passed in
        try {
            inputStream = new FileInputStream(inputFile); // open input stream

            file = new File(inputFile);
            packetSize = (int) file.length() / numOfFrames;

            address = InetAddress.getByName(receiverAddress); // convert receiverAddress to an InetAddress
            serverSocket = new DatagramSocket(); // Instantiate the datagram socket
            byte[] dataToSend = new byte[packetSize]; // create the "send" buffer
            byte[] dataToReceive = new byte[packetSize]; // create the "receive" buffer

            // logging counters/variables
            int packetCount = 0;
            int bytesRead;
            long startOffset = 0;
            long endOffset = 0;

            System.out.println("\nSENDING FILE\n");
            do {
                // read the input file in packetSize chunks, and send them to the server
                bytesRead = inputStream.read(dataToSend);
                if (bytesRead == -1) {
                    // end of file, tell the receiver that we are done sending
                    dataToSend = "end".getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(dataToSend, 3, address, receiverPort);
                    serverSocket.send(datagramPacket);
                    System.out.println("Sent end packet.  Terminating.");
                    break;
                } else {
                    endOffset += bytesRead;
                    System.out.format("Packet: %4d  -  Start Byte Offset: %8d  -  End Byte Offset: %8d%n",
                            ++packetCount, startOffset, endOffset);
                    startOffset = endOffset;

                    // create and send the packet
                    DatagramPacket packet = new DatagramPacket(dataToSend, bytesRead, address, receiverPort);
                    serverSocket.send(packet);
                    dataToSend = new byte[packetSize]; // flush buffer

                    while (true) {
                        // Receive the server's packet
                        DatagramPacket received = new DatagramPacket(dataToReceive, dataToReceive.length);
                        serverSocket.receive(received);

                        // Get the message from the server's packet
                        String returnMessage = new String(received.getData()).trim();
                        if (returnMessage.equals("ok")) {
                            break;
                        } else {
                            System.out.println("Incorrect Ack");
                        }
                    }

                }
            } while (true);
            // done, close streams/sockets
            inputStream.close();
            serverSocket.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\n\nUNABLE TO LOCATE OR OPEN THE INPUT FILE: " + inputFile + "\n\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
