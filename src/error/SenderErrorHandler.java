package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static util.Constants.ACK_RECEIVED;
import static util.Utility.makeStringDatagram;
import static validation.SenderValidator.validatePacketFromReceiver;

public class SenderErrorHandler {

    private static final int MAX_RETRY = 3;
    private static int currentRetry = 0;

    public SenderErrorHandler() {
    }

    public void resetRetries() {
        currentRetry = 0;
    }

    public void resendPacket(DatagramSocket serverSocket, DatagramPacket datagramToResend, byte[] dataToReceive,
        long endOffset, long previousOffset, int bytesRead, int packetCount)
        throws IOException, ClassNotFoundException {

        while (currentRetry++ < MAX_RETRY) {
            serverSocket.send(makeStringDatagram("error", datagramToResend.getAddress(), datagramToResend.getPort()));
            System.out.println("\t\tExecuting packet retry attempt: " + currentRetry + "/" + MAX_RETRY);
            serverSocket.send(datagramToResend);

            String ackFromReceiver = validatePacketFromReceiver(serverSocket, dataToReceive, endOffset, previousOffset, bytesRead,
                packetCount);

            if (ackFromReceiver.equalsIgnoreCase(ACK_RECEIVED)) {
                break;
            }

        }
        if (currentRetry >= MAX_RETRY) {
            System.out.println("\t\tPacket retry failed, stopping program");
            serverSocket.send(makeStringDatagram("stop", datagramToResend.getAddress(), datagramToResend.getPort()));
            System.exit(400);
        }
    }
}
