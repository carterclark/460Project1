package example;

import java.io.*;

import java.net.*;

import java.util.*;

/**
 * @author StarkeeCode
 */

public class ReceiverExample {

    public static void main(String args[]) {

        String host = "Serverhost";
        int index;
        try {
            ServerSocket ss2;
            ss2 = new ServerSocket(8000);
            Socket s1 = ss2.accept();
            DataInputStream dd1 = new DataInputStream(s1.getInputStream());

            Integer i1 = dd1.read();
            for (index = 0; index < i1; index++) {

                ServerSocket serverSocket;
                serverSocket = new ServerSocket(9000 + index);
                Socket socket = serverSocket.accept();
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String dataUTF = dataInputStream.readUTF();

                System.out.println(dataUTF);
                System.out.println("Frame " + index + " received");
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.write(index);
                System.out.println("ACK sent for " + index);
            }
        } catch (Exception ex) {
            System.out.println("Error" + ex);
        }
    }
}