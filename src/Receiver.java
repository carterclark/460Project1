
import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;

public class Receiver {// Server

	private static DatagramSocket socket;
    private static byte[] buf = new byte[4096];
	
	public static void main(String[] args) {

		FileOutputStream outputStream = null;
		
		// logging counters/variables
		int packetCount = 0;
        long startOffset = 0; 
        long endOffset = 0;
		
        if(args.length < 2) {
        	System.out.println("\n\nERROR: you must specify the port and the new file name.  Example: java Receiver 5656 some-new-file.jpg");
        	System.exit(1);
        }
        
		try {

			// initialize socket and create output stream
			socket = new DatagramSocket(Integer.parseInt(args[0]));

			System.out.println("\nWAITING FOR FILE\n");
			while(true) {
				DatagramPacket packet = new DatagramPacket(buf, buf.length); // datagram to hold incoming packet
				socket.receive(packet); // wait for a packet
				
            	endOffset += packet.getLength(); // endOffset accumulates with length of data in packet, offsets are relative to the file not the buffer
            	if(new String(packet.getData()).trim().equals("end")) {
                	System.out.println("Received end packet.  Terminating.");
	            	break;
	            } else {
	            	// if output stream is not initialized do it now
	                if(outputStream == null) {
	                	outputStream = new FileOutputStream(args[1]); 
	                }
		        	System.out.format("Packet: %4d  -  Start Byte Offset: %8d  -  End Byte Offset: %8d%n", ++packetCount, startOffset, endOffset); // progress logging
	            	outputStream.write(packet.getData(), 0, packet.getLength());
	            	startOffset = endOffset; // start offset of next packet will be end offset of current packet, offsets are relative to the file not the buffer
	            }
	            
	            buf = new byte[4096]; // flush buffer
			}
			
			// done, close sockets/streams
			socket.close();
			outputStream.close();
		
		} catch (SocketException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
