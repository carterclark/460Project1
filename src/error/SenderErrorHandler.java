package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import objects.Packet;

import static util.Utility.convertByteArrayToPacket;
import static util.Utility.makeStringDatagram;

public class SenderErrorHandler {

    private static final int MAX_RETRY = 3;
    private static int currentRetry = 0;

    public SenderErrorHandler() {
    }

    public void resetRetries(){
        currentRetry = 0;
    }

    public void sendPacket(DatagramSocket serverSocket, DatagramPacket datagramToResend)
        throws IOException, ClassNotFoundException {

        if (currentRetry++ < MAX_RETRY) {
            serverSocket.send(makeStringDatagram("error", datagramToResend.getAddress(), datagramToResend.getPort()));

            System.out.println("\t\tExecuting packet retry attempt: " + currentRetry + "/" + MAX_RETRY);
            System.out.println("Packet: " + convertByteArrayToPacket(datagramToResend.getData()));

            serverSocket.send(datagramToResend);
        } else {
            System.out.println("\t\tPacket retry failed, closing program");
            System.exit(400);
        }

    }
}
