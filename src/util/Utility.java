package util;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.Data;

import objects.Packet;

public class Utility {

    public static final String SENT = "SENT";
    public static final String DROP = "DROP";
    public static final String ERR = "ERR";
    public static final String RECV = "RECV";

    public static final String DUPLICATE_ACK = "DuplAck";
    public static final String ERROR_ACK = "ErrAck";
    public static final String TIMEOUT = "TimeOut";
    
    public static int rngErrorGenerator() {
        // Gives a random number between 1-50
//        return (int) Math.floor(Math.random() * (50 + 1) + 1);
        return 50;
    }


    public static byte[] convertPacketToByteArray(Packet packet) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(packet);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static Packet convertByteArrayToPacket(byte[] data) throws IOException, ClassNotFoundException {

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInput input = new ObjectInputStream(bais);
        try{
            return (Packet) input.readObject();
        } catch (EOFException exception){
            System.out.println("In util catch block");
            exception.printStackTrace();
        }
        return null;
    }

    public static Packet makeGenericPacket(){
        short goodChecksum = 0;
        short goodLength = 0;
        int ack = 0;
        int seqNo = 0;
        byte[] data = "Success".getBytes(StandardCharsets.UTF_8);

        return new Packet(goodChecksum, goodLength, ack, seqNo, data);
    }

    public static DatagramPacket convertPacketToDatagram(Packet packet, InetAddress inetAddress, int port) throws IOException
    {
        byte[] packetData = convertPacketToByteArray(packet);
        return new DatagramPacket(packetData, packetData.length, inetAddress, port);
    }

//    public static int getNonEmptyLengthOfByteArray(byte[] dataSet){
//        int count = 0;
//        for(byte data : dataSet){
//            if(data ){
//                count++
//            }
//        }
//
//    }
}
