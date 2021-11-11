import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;

import static util.Constants.MAX_PACKET_SIZE;
import static util.Utility.Usage;
import static util.Utility.makeSpaces;

public class SenderBase {

    protected static String receiverAddress = "";
    protected static String inputFile = "";
    protected static double dataGrams = 0.0;
    protected static int numOfFrames = 15;
    protected static int dataSize = MAX_PACKET_SIZE;
    protected static int timeOut = 300; // default timeout
    protected static int receiverPort = 0;
    protected long startTime;

    protected FileInputStream inputStream;
    protected File file;

    // for sending packets
    protected InetAddress address;
    protected DatagramSocket serverSocket;
    protected DatagramPacket datagramToSend;
    protected byte[] dataToSend;
    protected int bytesRead;
    protected long previousOffset;
    protected int packetCount;

    // parse the command line parameters
    protected static void ParseCmdLine(String[] args, boolean overrideParse) {
        int i = 0;
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

            while (i < args.length) {
                arg = args[i];

                // process any command line switches
                if (arg.startsWith("-")) {

                    // optional parameters
                    switch (arg.charAt(1)) {
                        case 'd':
                            // if next argument also starts with a - then the value for the command line
                            // switch was not provided
                            if (args[i + 1].startsWith("-")) {
                                System.err.println("-d requires a value");
                                Usage();
                            } else {
                                dataGrams = Double.parseDouble(args[++i]);
                            }
                            break;
                        case 's':
                            if (args[i + 1].startsWith("-")) {
                                System.err.println("-s requires a packet size");
                                Usage();
                            } else {
                                dataSize = Integer.parseInt(args[++i]);
                                if (dataSize > 4096) {
                                    System.err.println("Packetsize cannot be greater than 4096");
                                    Usage();
                                }
                            }
                            break;
                        case 't':
                            if (args[i + 1].startsWith("-")) {
                                System.err.println("-t requires a timeout value");
                                Usage();
                            } else {
                                timeOut = Integer.parseInt(args[++i]);
                            }
                            break;
                    }
                    // mandatory parameters
                } else {
                    // not a command line switch so must be the filename, receiver address, or
                    // receiver port
                    // must have at minimum the filename, the receiver address, and the receiver
                    // port
                    if (i == (args.length - 3)) {
                        receiverAddress = args[i];
                    }

                    if (i == (args.length - 2)) {
                        receiverPort = Integer.parseInt(args[i]);
                    }

                    if (i == (args.length - 1)) {
                        inputFile = args[i];
                    }
                    i++;
                }
            }

            // if values were not provided on commandline the defaults will trigger a usage
            // message
            if (Objects.equals(inputFile, "") || Objects.equals(receiverAddress, "") | receiverPort == 0) {
                Usage();
            }
        }
    }

    protected void printSenderInfo(String senderAction, int packetCount, long previousOffset, long endOffset,
        long startTime, String senderCondition) {
        System.out.printf("%s:\t%s%d:%sTime Sent:%s" + "%s\n", makeSpaces(senderAction), makeSpaces(packetCount),
            previousOffset, makeSpaces(endOffset), makeSpaces(System.currentTimeMillis() - startTime),
            makeSpaces(senderCondition));
    }

}
