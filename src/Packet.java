
public class Packet {
    short checkSum; // 16-bit 2-byte
    short length; // 16-bit 2-byte
    int ack; // 32-bit 4-byte
    int seqNo; // 32-bit 4-byte Data packet Only
    byte[] data; // 0-500 bytes. Data packet only. Variable

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
}
