package se.omfilm.gameboy.internal.memory;

public class ByteArrayMemory implements Memory {
    private final int offset;
    private final byte[] data;

    public ByteArrayMemory(int offset, byte[] data) {
        this.data = data;
        this.offset = offset;
    }

    public ByteArrayMemory(byte[] data) {
        this(0, data);
    }

    public int readByte(int address) {
        return unsigned(this.data[address - offset]);
    }

    public void writeByte(int address, int data) {
        this.data[address - offset] = signed(data);
    }

    private int unsigned(byte input) {
        return input & 0xFF;
    }

    private byte signed(int input) {
        return (byte) input;
    }
}
