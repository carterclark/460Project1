package util;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import javax.xml.crypto.Data;

import objects.Packet;

public class Utility {

    public static void Usage() {
        System.out.println("\n\nMandatory command parameters must be entered in the order displayed here.");
        System.out.println("Parameters in [] are optional and must come before the three mandatory items.");
        System.out.println("-d is the percentage of packets to alter.  -d 2.5 (not implemented)");
        System.out.println("-s is packet size, cannot exceed 4096.  -s 512 (default is 4096)");
        System.out.println("-t is the timeout value.  -t 300 (not implemented)");
        System.out.println("Usage: java Sender [-d #.#] [-s ###] [-t ###] receiver_address receiver_port input_file");
        System.exit(1);
    }

    public static int rngErrorGenerator() {
        // Gives a random number between 1-50
        return (int) Math.floor(Math.random() * (50 + 1) + 1);
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


        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInput input = new ObjectInputStream(bais);
            return (Packet) input.readObject();
        } catch (EOFException exception) {
            System.out.println("In util catch block");
            exception.printStackTrace();
        }
        return null;
    }

    public static Packet makeGenericPacket() {
        short goodChecksum = 0;
        short goodLength = 0;
        int ack = 0;
        int seqNo = 0;
        byte[] data = "Success".getBytes(StandardCharsets.UTF_8);

        return new Packet(goodChecksum, goodLength, ack, seqNo, data);
    }

    public static DatagramPacket convertPacketToDatagram(Packet packet, InetAddress inetAddress, int port)
        throws IOException {
        byte[] packetData = convertPacketToByteArray(packet);
        return new DatagramPacket(packetData, packetData.length, inetAddress, port);
    }

    public static DatagramPacket makeStringDatagram(String stringToSend, InetAddress inetAddress, int port) {
        byte[] data = stringToSend.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(data, data.length, inetAddress, port);
    }

    public static String makeSpaces(Object object){
        StringBuilder string = new StringBuilder(String.valueOf(object));
        int numSpaces = 6 - string.length();
        string.append(" ".repeat(Math.max(0, numSpaces)));

        return string.toString();
    }
}
