package error;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static util.Constants.ACK_RECEIVED;
import static util.Constants.CORRUPT;
import static util.Constants.CORRUPTED_ACK;
import static util.Constants.DROP;
import static util.Constants.ERR;
import static util.Constants.ERR_ACK;
import static util.Constants.MAX_RETRY;
import static util.Constants.MOVE_WINDOW;
import static util.Constants.OUT_OF_SEQUENCE;
import static util.Constants.RESENDING;
import static util.Constants.SENT;
import static util.Utility.makeStringDatagram;
import static util.Utility.printSenderInfo;
import static validation.SenderValidator.validatePacketFromReceiver;

public class SenderErrorHandler {

    private static int currentRetry = 0;

    public SenderErrorHandler() {
    }

    public void resetRetries() {
        currentRetry = 0;
    }

    public void resendPacket(DatagramSocket serverSocket, DatagramPacket datagramToResend, byte[] dataToReceive,
        long endOffset, long previousOffset, int bytesRead, int packetCount, long startTime,
        String validationFromReceiver) throws IOException, ClassNotFoundException {

        while (currentRetry++ < MAX_RETRY) {

            // If the packet was corrupted then it was never added to the packet array, and we don't want to send the
            // error datagram which deletes the most recent packet from the array
            if (!validationFromReceiver.equals(ERR_ACK)) {
                serverSocket.send(
                    makeStringDatagram("error", datagramToResend.getAddress(), datagramToResend.getPort()));
            }
            serverSocket.send(datagramToResend);

            String ackFromReceiver =
                validatePacketFromReceiver(serverSocket, dataToReceive, endOffset, previousOffset, bytesRead,
                    packetCount);

            printSenderInfo(RESENDING, packetCount, previousOffset, endOffset, startTime, SENT,
                ackFromReceiver);

            if (ackFromReceiver.equalsIgnoreCase(MOVE_WINDOW)) {
                break;
            }
        }
        if (currentRetry >= MAX_RETRY) {
            System.out.printf("\n\t\tPacket retry failed after %d attempts, stopping program", currentRetry);
            serverSocket.send(makeStringDatagram("stop", datagramToResend.getAddress(), datagramToResend.getPort()));
            System.exit(400);
        }
    }
}
