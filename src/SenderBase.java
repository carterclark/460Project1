import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;

import static util.Constants.MAX_PACKET_SIZE;
import static util.Constants.TIMEOUT_MAX;
import static util.Utility.Usage;

public class SenderBase {

    protected static String receiverAddress = "";
    protected static String inputFile = "";
    protected static double percentOfDataToCorrupt = 0;
    protected static int numOfFrames = 20;
    protected static int dataSize = MAX_PACKET_SIZE;
    protected static long timeOut = TIMEOUT_MAX; // default timeout
    protected static int receiverPort = 0;
    protected long startTime;

    protected FileInputStream inputStream;
    protected File file;

    // for sending packets
    protected InetAddress address;
    protected DatagramSocket socketToReceiver;
    protected DatagramPacket datagramWithData;
    protected byte[] dataFromFile;
    protected int bytesRead;
    protected long previousOffset;
    protected int packetCount;

    // parse the command line parameters
    protected static void ParseCmdLine(String[] args, boolean overrideParse) {
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
