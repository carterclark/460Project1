import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;

import static util.Utility.MAX_PACKET_SIZE;

/**
 * On the sender side, we have to simulate sending bad amounts of data as well.
 */
public class SenderBase {

    protected static String receiverAddress = "";
    protected static String inputFile = "";
    protected static double dataGrams = 0.0;
    protected static int numOfFrames = 15;
    protected static int dataSize = MAX_PACKET_SIZE;
    protected static int packetSize = MAX_PACKET_SIZE;
    protected static int timeOut = 300; // default timeout
    protected static int receiverPort = 0;
    protected long startTime;

    protected FileInputStream inputStream;
    protected File file;

    // for sending packets
    protected InetAddress address;
    protected DatagramSocket serverSocket;
    protected DatagramPacket datagramPacketToSend;
    protected byte[] dataToSend;
    protected int bytesRead;
    protected long previousOffset;
    protected int packetCount;

    // parse the command line parameters
    protected static void ParseCmdLine(String[] args) {
        int i = 0;
        String arg;

        if (args.length < 3) {
            Usage(); // run with no parameters or too few to see usage message
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

    // directions for use
    protected static void Usage() {
        System.out.println("\n\nMandatory command parameters must be entered in the order displayed here.");
        System.out.println("Parameters in [] are optional and must come before the three mandatory items.");
        System.out.println("-d is the percentage of packets to alter.  -d 2.5 (not implemented)");
        System.out.println("-s is packet size, cannot exceed 4096.  -s 512 (default is 4096)");
        System.out.println("-t is the timeout value.  -t 300 (not implemented)");
        System.out.println("Usage: java Sender [-d #.#] [-s ###] [-t ###] receiver_address receiver_port input_file");
        System.exit(1);
    }

    protected void printSenderInfo(long endOffset, String senderCondition) {
        System.out.printf(
            "Packet: %d/%d - Start Byte Offset:%d" + " - End Byte Offset: %d - Sent time:%d - " + senderCondition +
                "\n",
            packetCount, numOfFrames, previousOffset, endOffset, (System.currentTimeMillis() - startTime));
    }

}
