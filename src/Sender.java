import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import error.SenderErrorHandler;
import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.GOOD_CHECKSUM;
import static util.Constants.SENDING;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.makeStringDatagram;
import static util.Utility.printSenderInfo;
import static validation.SenderValidator.validatePacketFromReceiver;

public class Sender extends SenderBase {// Client

    private final SenderErrorHandler errorHandler = new SenderErrorHandler();

    public static void main(String[] args) {
        Sender sender = new Sender();
        sender.run(args);
    }

    public void run(String[] args) {
        ParseCmdLine(args, true); // parse the parameters that were passed in
        boolean isFirstRun = true;
        try {
            inputStream = new FileInputStream(inputFile); // open input stream

            file = new File(inputFile);
            dataSize = (int) file.length() / numOfFrames++;

            address = InetAddress.getByName(receiverAddress); // convert receiverAddress to an InetAddress
            serverSocket = new DatagramSocket(); // Instantiate the datagram socket
            dataToSend = new byte[dataSize]; // create the "send" buffer
            byte[] dataToReceive = new byte[dataSize]; // create the "receive" buffer

            // logging counters/variables
            packetCount = 1;
            previousOffset = 0;
            long endOffset = 0;
            byte[] packetDataToSend;

            System.out.println("\nStarting Sender\n");
            do {
                startTime = System.currentTimeMillis();
                // read the input file in packetSize chunks, and send them to the server
                bytesRead = inputStream.read(dataToSend);
                if (bytesRead == -1) {
                    packetDataToSend = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, new byte[0]));
                    datagramToSend =
                        new DatagramPacket(packetDataToSend, packetDataToSend.length, address, receiverPort);
                    serverSocket.send(datagramToSend);

                    System.out.println("Sent end packet.  Terminating.");
                    break;
                } else {
                    endOffset += bytesRead;

                    packetDataToSend = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, dataToSend));
                    datagramToSend =
                        new DatagramPacket(packetDataToSend, packetDataToSend.length, address, receiverPort);
                    serverSocket.send(datagramToSend);

                    String ackFromReceiver =
                        validatePacketFromReceiver(serverSocket, dataToReceive, endOffset, previousOffset, bytesRead,
                            packetCount);

                    printSenderInfo(SENDING, packetCount, previousOffset, endOffset, startTime, ackFromReceiver);
                    //get acknowledgements from receiver
                    if (!ackFromReceiver.equalsIgnoreCase(ACK_RECEIVED)) {
                        errorHandler.resendPacket(serverSocket, datagramToSend, dataToReceive, endOffset,
                            previousOffset, bytesRead, packetCount, startTime);
                        errorHandler.resetRetries();
                    }

                    previousOffset = endOffset;
                    packetCount++;
                    dataToSend = new byte[dataSize]; // flush buffer
                    datagramToSend = null; // flush packet

                }
            } while (true);
            // done, close streams/sockets
            inputStream.close();
            serverSocket.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\n\nUNABLE TO LOCATE OR OPEN THE INPUT FILE: " + inputFile + "\n\n");
            System.out.println(ex);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
