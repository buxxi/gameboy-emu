package se.omfilm.gameboy.internal.memory;

public class ByteArrayMemory implements Memory {
    protected final int offset;
    protected final byte[] data;

    public ByteArrayMemory(int offset, byte[] data) {
        this.data = data;
        this.offset = offset;
    }

    public ByteArrayMemory(byte[] data) {
        this(0, data);
    }

    public int readByte(int address) {
        return unsigned(this.data[index(address)]);
    }

    public void writeByte(int address, int data) {
        this.data[index(address)] = signed(data);
    }

    private int unsigned(byte input) {
        return input & 0xFF;
    }

    private int index(int address) {
        return address - offset;
    }

    private byte signed(int input) {
        return (byte) input;
    }
}
