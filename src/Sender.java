import java.io.*;

public class Sender {

	private static String receiverAddress; 
	private static String inputFile;
	private static double dataGrams = 0.0;
	private static int packetSize = 4096; // default buffer will send the data in 4K chunks
	private static int timeOut = 300; // default timeout
	private static int receiverPort;
	
	private static void Usage() {
		System.out.println("Arguments in [] are optional.  Any arguments used must be entered in the order displayed here.");
		System.out.println("Usage: Sender [-d #.#] [-s ###] [-t ###] -f inputfile reciever_ip reciever_port");
	}
	
	private static void ParseCmdLine(String[] args) {
		int i = 0;
		boolean haveFilename = false;
		String arg;

		while (i < args.length) {
			arg = args[i];
			
			// process any command line switches
			if(arg.startsWith("-")) {

				switch(arg.charAt(1)) {
					case 'd':
						// if next argument also starts with a - then the value for the command line switch was not provided
						if (i < args.length && args[i + 1].startsWith("-")) {
							System.err.println("-d requires a value");
							Usage();
							System.exit(4);
						} else {
							dataGrams = Double.parseDouble(args[++i]);
						}
						break;	
					case 'f':
						// must have at minimum -f and the filename, the receiver address, and the reciever port
						// this check that there is a filename provided along with the address and port
						if (i == (args.length - 3) || args[i + 1].startsWith("-")) {
							System.err.println("-f requires a filename");
							Usage();
							System.exit(3);
						} else {
							inputFile = args[++i];
							haveFilename = true; // filename is required
						} 
						break;
					case 's':
						if (i < args.length && args[i + 1].startsWith("-")) {
							System.err.println("-s requires a packet size");
							Usage();
							System.exit(2);
						} else {
							packetSize = Integer.parseInt(args[++i]);
						}
						break;
					case 't':
						if (i < args.length && args[i + 1].startsWith("-")) {
							System.err.println("-t requires a timeout value");
							Usage();
							System.exit(1);
						} else {
							timeOut = Integer.parseInt(args[++i]);
						}
						break;
				}
			} else {
			    // not a command line switch so must be the address or port
				if(i == (args.length - 2)) {
					receiverAddress = args[i];
				}
				
				if(i == (args.length - 1)) {
					receiverPort = Integer.parseInt(args[i]);
				}
				i++;
			}
		}
		
		// must provide the filename
		if (haveFilename == false)
			Usage();
	}
	
	public static void main(String[] args) {
		String outputFile;
		
		ParseCmdLine(args);
		
		// for testing purposes
		outputFile = "new_" + inputFile;

		System.out.println("The packet size will be: " + packetSize);
		System.out.println("The source file is     : " + inputFile);
		System.out.println("The timeout is         : " + timeOut);
		System.out.println("Datagram percentage is : " + dataGrams);
		System.out.println("Receiver address is    : " + receiverAddress); 
		System.out.println("Receiver port is       : " + receiverPort);
		System.out.println("Output filename is     : " + outputFile);
		
		try (
				InputStream inputStream = new FileInputStream(inputFile);
	            OutputStream outputStream = new FileOutputStream(outputFile); // for testing purposes 
	        ) {
	 
	        byte[] buffer = new byte[packetSize];
	 
	        while (inputStream.read(buffer) != -1) {
	        	
	        	// THIS WILL BE WHERE THE DATA SEND TAKES PLACE
	            outputStream.write(buffer); // for testing purposes
	            
	            
	        }
	 
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
		
	}
}
