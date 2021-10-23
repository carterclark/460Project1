import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

public class SenderUtil {
    protected static String receiverAddress = "";
    protected static String inputFile = "";
    protected static double dataGrams = 0.0;
    protected static int numOfFrames = 1;
    protected static int maxPacketSize = 4096; // default buffer will send the data in 4K chunks
    protected static int packetSize = maxPacketSize / numOfFrames;
    protected static int timeOut = 300; // default timeout
    protected static int receiverPort = 0;

    protected FileInputStream inputStream;
    protected File file;

    // parse the command line parameters
    protected static void ParseCmdLine(String[] args) {
        int i = 0;
        String arg;

        if (args.length < 3)
            Usage(); // run with no parameters or too few to see usage message

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
                            maxPacketSize = Integer.parseInt(args[++i]);
                            if (maxPacketSize > 4096) {
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
                if (i == (args.length - 4)) {
                    receiverAddress = args[i];
                }

                if (i == (args.length - 3)) {
                    receiverPort = Integer.parseInt(args[i]);
                }

                if (i == (args.length - 2)) {
                    inputFile = args[i];
                }
                if (i == (args.length - 1)) {
                    numOfFrames = Integer.parseInt(args[i]) - 1;
                }
                i++;
            }
        }

        // if values were not provided on commandline the defaults will trigger a usage
        // message
        if (Objects.equals(inputFile, "") || Objects.equals(receiverAddress, "") | receiverPort == 0)
            Usage();
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

}
