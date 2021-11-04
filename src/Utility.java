import java.io.*;

public class Utility {

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
