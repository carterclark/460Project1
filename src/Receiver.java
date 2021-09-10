

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver {// Server

    public static void main(String[] args) throws IOException{

        //Opens a datagram socket on the specified port
        DatagramSocket ds = new DatagramSocket(5000);

        byte[] buf = new byte[1024];

        //Constructs a datagram packet for receiving the packets of specified length
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        ds.receive(dp);
        String str = new String(dp.getData(), dp.getLength());

        System.out.println(str);
        // changes
    }
}
