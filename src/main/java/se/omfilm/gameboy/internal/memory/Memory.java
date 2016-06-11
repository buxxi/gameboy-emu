package se.omfilm.gameboy.internal.memory;

public interface Memory {
    int readByte(int address);

    void writeByte(int address, int data);

    default int readWord(int address) {
        return readByte(address) + ((readByte(address + 1) << 8));
    }

    default void writeWord(int address, int data) {
        writeByte(address, data & 0xFF);
        writeByte(address + 1, data >> 8);
    }
}
