import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Objects;

import error.SenderErrorHandler;
import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.GOOD_CHECKSUM;
import static util.Constants.MAX_PACKET_SIZE;
import static util.Constants.SENDING;
import static util.Constants.TIMEOUT;
import static util.Constants.TIMEOUT_MAX;
import static util.Utility.Usage;
import static util.Utility.convertPacketToByteArray;
import static util.Utility.getCorruptedData;
import static util.Utility.printSenderInfo;
import static util.Utility.rngErrorGenerator;
import static validation.SenderValidator.validatePacketFromReceiver;

public class Sender {// Client

    private final SenderErrorHandler errorHandler = new SenderErrorHandler();

    private static String receiverAddress = "";
    private static String inputFile = "";
    private static double percentOfDataToCorrupt = 0;
    private static int numOfFrames = 20;
    private static int dataSize = MAX_PACKET_SIZE;
    private static long timeOut = TIMEOUT_MAX; // default timeout
    private static int receiverPort = 0;
    private long startTime;

    private FileInputStream inputStream;
    private File file;

    // for sending packets
    private InetAddress address;
    private DatagramSocket socketToReceiver;
    private DatagramPacket datagramWithData;
    private byte[] dataFromFile;
    private int bytesRead;
    private long previousOffset;
    private int packetCount;

    public static void main(String[] args) {
        Sender sender = new Sender();
        sender.run(args);
    }

    public void run(String[] args) {
        ParseCmdLine(args, false); // parse the parameters that were passed in
        try {
            inputStream = new FileInputStream(inputFile); // open input stream

            file = new File(inputFile);

            if (dataSize == MAX_PACKET_SIZE) {
                dataSize = (int) file.length() / numOfFrames++;
            }

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
                        validatePacketFromReceiver(socketToReceiver, dataToReceive, endOffset, previousOffset,
                            bytesRead, packetCount);

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

    // parse the command line parameters
    private static void ParseCmdLine(String[] args, boolean overrideParse) {
        int index = 0;
        String arg;

        if (overrideParse) {
            receiverAddress = "localhost";
            receiverPort = 8080;
            inputFile = "src/image.png";
        } else {
            if (args.length < 3) {
                System.out.println("\n\nINSUFFICIENT COMMAND LINE ARGUMENTS\n\n");
                Usage();
            }

            while (index < args.length) {
                arg = args[index];

                // process any command line switches
                if (arg.startsWith("-")) {

                    // optional parameters
                    switch (arg.charAt(1)) {
                        case 'd':
                            if (args[index + 1].startsWith("-")) {
                                System.err.println("-d requires a percent (as decimal) to corrupt");
                                Usage();
                            } else {
                                percentOfDataToCorrupt = Double.parseDouble(args[++index]);
                            }
                            break;
                        case 's':
                            if (args[index + 1].startsWith("-")) {
                                System.err.println("-s requires a packet size");
                                Usage();
                            } else {
                                dataSize = Integer.parseInt(args[++index]);
                                if (dataSize > 4096) {
                                    System.err.println("packet size cannot be greater than 4096");
                                    Usage();
                                }
                            }
                            break;
                        case 't':
                            if (args[index + 1].startsWith("-")) {
                                System.err.println("-t requires a timeout value in seconds");
                                Usage();
                            } else {
                                timeOut = Integer.parseInt(args[++index]) * (long) 1000;
                            }
                            break;
                    }
                } else {

                    if (index == (args.length - 3)) {
                        receiverAddress = args[index];
                    }

                    if (index == (args.length - 2)) {
                        receiverPort = Integer.parseInt(args[index]);
                    }

                    if (index == (args.length - 1)) {
                        inputFile = args[index];
                    }
                    index++;
                }
            }

            // if values were not provided on commandline the defaults will trigger a usage
            // message
            if (Objects.equals(inputFile, "") || Objects.equals(receiverAddress, "") | receiverPort == 0) {
                Usage();
            }
        }
    }

}
