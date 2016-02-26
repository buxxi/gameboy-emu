package se.omfilm.gameboy;

public class ByteArrayMemory implements Memory {
    private final byte[] data;

    public ByteArrayMemory(byte[] data) {
        this.data = data;
    }

    public int readByte(int address) {
        return unsigned(this.data[address]);
    }

    public void writeByte(int address, int data) {
        this.data[address] = signed(data);
    }

    private int unsigned(byte input) {
        return input & 0xFF;
    }

    private byte signed(int input) {
        return (byte) input;
    }
}
