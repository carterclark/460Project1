import java.io.*;

public class deleteThis {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Packet packet = new Packet((short) 1, (short) 2, 3, 4, new byte[10]);

        byte[] bytes = convertPacketToByteArray(packet);

        Packet packet2 = (Packet) convertByteArrayToPacket(bytes);

        System.out.println("should be 3: " + packet2.getAck());
    }

    private static byte[] convertPacketToByteArray(Packet packet) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(packet);
        return byteArrayOutputStream.toByteArray();
    }

    protected static Object convertByteArrayToPacket(byte[] bytes) throws IOException, ClassNotFoundException {

        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();

        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }
}