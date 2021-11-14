import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import error.SenderErrorHandler;
import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.GOOD_CHECKSUM;
import static util.Constants.SENDING;
import static util.Constants.TIMEOUT;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.getCorruptedData;
import static util.Utility.printSenderInfo;
import static util.Utility.rngErrorGenerator;
import static validation.SenderValidator.validatePacketFromReceiver;

public class Sender extends SenderBase {// Client

    private final SenderErrorHandler errorHandler = new SenderErrorHandler();

    public static void main(String[] args) {
        Sender sender = new Sender();
        sender.run(args);
    }

    public void run(String[] args) {
        ParseCmdLine(args, false); // parse the parameters that were passed in
        try {
            inputStream = new FileInputStream(inputFile); // open input stream

            file = new File(inputFile);
            dataSize = (int) file.length() / numOfFrames++;

            address = InetAddress.getByName(receiverAddress); // convert receiverAddress to an InetAddress
            socketToReceiver = new DatagramSocket(); // Instantiate the datagram socket
            socketToReceiver.setSoTimeout((int) timeOut);
            dataFromFile = new byte[dataSize]; // create the "send" buffer
            byte[] dataToReceive = new byte[dataSize]; // create the "receive" buffer

            // logging counters/variables
            packetCount = 1;
            previousOffset = 0;
            long endOffset = 0;
            byte[] packetAsBytes;

            System.out.println("\nStarting Sender\n");
            do {
                startTime = System.currentTimeMillis();
                // read the input file in packetSize chunks, and send them to the server
                bytesRead = inputStream.read(dataFromFile);
                if (bytesRead == -1) {
                    packetAsBytes = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, new byte[0]));
                    datagramWithData = new DatagramPacket(packetAsBytes, packetAsBytes.length, address, receiverPort);
                    socketToReceiver.send(datagramWithData);
                    System.out.println("Sent end packet.  Terminating.");
                    break;
                } else {
                    endOffset += bytesRead;

                    packetAsBytes = convertPacketToByteArray(
                        new Packet(GOOD_CHECKSUM, bytesRead, endOffset, packetCount, dataFromFile));
                    datagramWithData = new DatagramPacket(packetAsBytes, packetAsBytes.length, address, receiverPort);

                    if (percentOfDataToCorrupt > 0 && rngErrorGenerator() < 15) { //simulate corruption based on user
                        // input
                        byte[] corruptedData = getCorruptedData(packetAsBytes, percentOfDataToCorrupt);
                        DatagramPacket corrupted =
                            new DatagramPacket(corruptedData, corruptedData.length, address, receiverPort);
                        corrupted.setData(corruptedData);
                        socketToReceiver.send(corrupted);
                        percentOfDataToCorrupt = 0; // end error sim after one iteration
                    } else {
                        socketToReceiver.send(datagramWithData);
                    }

                    String validationFromReceiver =
                        validatePacketFromReceiver(socketToReceiver, dataToReceive, endOffset, previousOffset, bytesRead,
                            packetCount);

                    printSenderInfo(SENDING, packetCount, previousOffset, endOffset, startTime, validationFromReceiver);

                    //get acknowledgements from receiver
                    if (!validationFromReceiver.equalsIgnoreCase(ACK_RECEIVED)) {
                        errorHandler.resendPacket(socketToReceiver, datagramWithData, dataToReceive, endOffset,
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
            socketToReceiver.close();
        } catch (FileNotFoundException ex) {
            System.out.println("\n\nUNABLE TO LOCATE OR OPEN THE INPUT FILE: " + inputFile + "\n\n");
            System.out.println(ex);
        } catch (SocketTimeoutException ex) {
            System.out.println(TIMEOUT + " On Sequence " + packetCount);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
