package example;


import java.io.*;

import java.net.*;

import java.util.Scanner;

/**
 * @author StarkeeCode
 */

public class SenderExample {

    public static void main(String args[]) {

        int portOne = 9000, index, portTwo = 8000;
        String localhost = "localhost";
        DataOutputStream outputStream;
        String fullMessage = "";

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter number of frames : ");
            int number = scanner.nextInt();

            if (number == 0) {
                System.out.println("No frame is sent");

            } else {
                Socket socketTwo;
                socketTwo = new Socket(localhost, portTwo);
                outputStream = new DataOutputStream(socketTwo.getOutputStream());
                outputStream.write(number);
            }

            for (index = 0; index < number; index++) {
                System.out.print("Enter message : ");
                String inputString = scanner.next();
                System.out.println("Frame " + index + " is sent");

                Socket socketOne;
                socketOne = new Socket(localhost, portOne + index);
                outputStream = new DataOutputStream(socketOne.getOutputStream());
                outputStream.writeUTF(inputString);

                DataInputStream inputStream = new DataInputStream(socketOne.getInputStream());
                Integer currentFrame = inputStream.read();
                System.out.println("Acknowledgement for :" + currentFrame + " is  received");
            }

        } catch (Exception ex) {
            System.out.println("ERROR :" + ex);
        }

    }

}