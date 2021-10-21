package util;

import java.lang.*;

import static java.lang.Byte.valueOf;

public class Util {

    public static void main(String[] args) {
        System.out.println("      d    hello     d  ".trim());
    }
    public static String removeSpaces(String input) {
        byte[] buffer = new byte[256];
        int lastGoodChar = 0;

        //fill it up for example only
        byte[] fillBuffer = (input.getBytes());
        System.arraycopy(fillBuffer, 0, buffer, 0, fillBuffer.length);

        //Now remove extra bytes from "buf"
        for (int i = 0; i < buffer.length; i++) {
            int bufferIntValue = valueOf(buffer[i]).intValue();
            if (bufferIntValue == 0) {
                lastGoodChar = i;
                break;
            }
        }
        return new String(buffer, 0, lastGoodChar);
    }
}
