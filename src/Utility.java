import java.io.*;

public class Utility {

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
}
