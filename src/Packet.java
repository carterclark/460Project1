
public class Packet {
    short checkSum; // 16-bit 2-byte
    short length; // 16-bit 2-byte
    int ack; // 32-bit 4-byte
    int seqNo; // 32-bit 4-byte Data packet Only
    byte[] data; // 0-500 bytes. Data packet only. Variable
}
