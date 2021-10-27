import java.io.Serializable;

public class Packet implements Serializable {
    private short checkSum; // 16-bit 2-byte
    private short length; // 16-bit 2-byte
    private int ack; // 32-bit 4-byte
    private int seqNo; // 32-bit 4-byte Data packet Only
    private byte[] data; // 0-500 bytes. Data packet only. Variable

    public Packet(short checkSum, short length, int ack, int seqNo, byte[] data) {
        this.checkSum = checkSum;
        this.length = length;
        this.ack = ack;
        this.seqNo = seqNo;
        this.data = data;
    }

    public short getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(short checkSum) {
        this.checkSum = checkSum;
    }

    public short getLength() {
        return length;
    }

    public void setLength(short length) {
        this.length = length;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "checkSum: " + checkSum
                + ", length: " + length
                + ", ack: " + ack
                + ", seqNo: " + seqNo
                + ", data.length: " + data.length;
    }
}
