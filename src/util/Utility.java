package util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Random;

import jdk.swing.interop.SwingInterOpUtils;
import objects.Packet;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.CORRUPT;
import static util.Constants.CORRUPTED_ACK;
import static util.Constants.DUPL;
import static util.Constants.DUP_ACK;
import static util.Constants.ERR;
import static util.Constants.ERR_ACK;
import static util.Constants.MOVE_WINDOW;
import static util.Constants.OUT_OF_SEQUENCE;
import static util.Constants.RECEIVED;
import static util.Constants.SENT;
import static util.Constants.STATUS_ARRAY;

public class Utility {

    public static void printSenderInfo(String senderAction, int packetCount, long previousOffset, long endOffset,
        long startTime, String datagramCondition, String ackFromReceiver) {

        if (ackFromReceiver.equals(CORRUPT)) {
            ackFromReceiver = ERR_ACK;
        } else if (ackFromReceiver.equals(ACK_RECEIVED)) {
            ackFromReceiver = MOVE_WINDOW;
        }

        DecimalFormat df = new DecimalFormat("0000");
        //Sending:1	0:464	Time Sent: 31 AckR
        System.out.printf("%s:\t%s\t%s:%s\tTime Sent:\s%s\t%s\t%s %s %s\n", senderAction, packetCount,
            df.format(previousOffset), endOffset, System.currentTimeMillis() - startTime, datagramCondition,
            ACK_RECEIVED, packetCount, ackFromReceiver);
    }

    public static void printReceiverInfo(String receiverStatus, long startTime, int packetCount, String ackFromSender) {

        switch (ackFromSender) {
            case DUP_ACK -> {
                receiverStatus = DUPL;
                ackFromSender = OUT_OF_SEQUENCE;
            }
            case ERR_ACK -> ackFromSender = ERR;
            default -> ackFromSender = RECEIVED;
        }

        System.out.printf("\n%s:\t%s%s%s", receiverStatus, makeSpaces(System.currentTimeMillis() - startTime),
            makeSpaces(packetCount), ackFromSender);
    }

    public static void Usage() {
        System.out.println("\n\nMandatory command parameters must be entered in the order displayed here."
            + "\nParameters in [] are optional and must come before the three mandatory items."
            + "\n-d is the percentage of packets to alter.  -d 2.5"
            + "\n-s is packet size, cannot exceed 4096.  -s 512 (default is 4096)"
            + "\n-t is the timeout value.  -t 300"
            + "\nUsage: java Sender [-d #.#] [-s ###] [-t ###] receiver_address receiver_port input_file");
        System.exit(1);
    }

    public static int randomNumberGenerator() {
        Random random = new Random();
        // Gives a random number between 1-100
        return random.nextInt(101) + 1;
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
            System.out.println("Exception encountered while reading packet, likely a byte array length issue");
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
        byte[] data = stringToSend.getBytes();
        return new DatagramPacket(data, stringToSend.length(), inetAddress, port);
    }

    public static String makeSpaces(Object object) {
        StringBuilder string = new StringBuilder(String.valueOf(object));
        int numSpaces = 7 - string.length();
        string.append(" ".repeat(Math.max(0, numSpaces)));

        return string.toString();
    }

    public static String getAckStatus(String ackMessage) {
        for (String ackStatus : STATUS_ARRAY) {
            if (ackMessage.contains(ackStatus)) {
                return ackStatus;
            }
        }

        return CORRUPTED_ACK;
    }

    public static byte[] getCorruptedData(byte[] oldData, double percentOfDataToCorrupt) {

        int newLength = (int) (oldData.length * percentOfDataToCorrupt);
        byte[] corruptedData = oldData.clone();

        for (int i = 0; i < newLength; i++) {
            corruptedData[i] = (byte) randomNumberGenerator();
        }

        return corruptedData;
    }
}
