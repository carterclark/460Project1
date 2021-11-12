import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import error.SenderErrorHandler;
import objects.Packet;

import static error.SenderErrorHandler.getCorruptedData;
import static util.Constants.ACK_RECEIVED;
import static util.Constants.GOOD_CHECKSUM;
import static util.Constants.SENDING;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.printSenderInfo;
import static validation.SenderValidator.validatePacketFromReceiver;

public class Sender extends SenderBase {// Client

    private final SenderErrorHandler errorHandler = new SenderErrorHandler();

    public static void main(String[] args) {
        Sender sender = new Sender();
        sender.run(args);
    }

    public void run(String[] args) {
        ParseCmdLine(args, false); // parse the parameters that were passed in
        boolean isFirstRun = true;
        try {
            inputStream = new FileInputStream(inputFile); // open input stream

            file = new File(inputFile);
            dataSize = (int) file.length() / numOfFrames++;

            address = InetAddress.getByName(receiverAddress); // convert receiverAddress to an InetAddress
            socketToSender = new DatagramSocket(); // Instantiate the datagram socket
            dataFromFile = new byte[dataSize]; // create the "send" buffer
            byte[] dataToReceive = new byte[dataSize]; // create the "receive" buffer

            // logging counters/variables
            packetCount = 1;
            previousOffset = 0;
            long endOffset = 0;
            byte[] packetAsBytes;
            byte[] corruptedData = new byte[0];

            System.out.println("\nStarting Sender\n");
            do {
                startTime = System.currentTimeMillis();
                // read the input file in packetSize chunks, and send them to the server
                bytesRead = inputStream.read(dataFromFile);
                if (bytesRead == -1) {
                    packetAsBytes = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, new byte[0]));
                    datagramWithData = new DatagramPacket(packetAsBytes, packetAsBytes.length, address, receiverPort);
                    socketToSender.send(datagramWithData);
                    System.out.println("Sent end packet.  Terminating.");
                    break;
                } else {
                    endOffset += bytesRead;

                    packetAsBytes = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, dataFromFile));
                    datagramWithData = new DatagramPacket(packetAsBytes, packetAsBytes.length, address, receiverPort);
                    DatagramPacket tempPacket =
                        new DatagramPacket(packetAsBytes, packetAsBytes.length, address, receiverPort);

                    //                    if(isFirstRun){
                    //                        percentOfDataToCorrupt = 0.25;
                    //                        isFirstRun = false;
                    //                    }

//                    if (percentOfDataToCorrupt > 0) { //simulate corruption based on user input
//                        corruptedData = getCorruptedData(corruptedData, tempPacket.getData(), percentOfDataToCorrupt);
//                        tempPacket.setData(corruptedData);
//                        percentOfDataToCorrupt = 0;
//                        socketToSender.send(tempPacket);
//                    } else {
//                        socketToSender.send(datagramWithData);
//                    }
                    socketToSender.send(datagramWithData);

                    String validationFromReceiver =
                        validatePacketFromReceiver(socketToSender, dataToReceive, endOffset, previousOffset, bytesRead,
                            packetCount);

                    printSenderInfo(SENDING, packetCount, previousOffset, endOffset, startTime, validationFromReceiver);

                    //get acknowledgements from receiver
                    if (!validationFromReceiver.equalsIgnoreCase(ACK_RECEIVED)) {
                        errorHandler.resendPacket(socketToSender, datagramWithData, dataToReceive, endOffset,
                            previousOffset, bytesRead, packetCount, startTime);
                        errorHandler.resetRetries();
                    }

                    previousOffset = endOffset;
                    packetCount++;
                    dataFromFile = new byte[dataSize]; // flush buffer
                    datagramWithData = null; // flush packet

                }
            } while (true);
            // done, close streams/sockets
            inputStream.close();
            socketToSender.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\n\nUNABLE TO LOCATE OR OPEN THE INPUT FILE: " + inputFile + "\n\n");
            System.out.println(ex);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
