package se.omfilm.gameboy.internal.memory;

/**
 * Represents a part of the memory model in the gameboy.
 * It can read/write unsigned bytes and words, so underlying implementations should make signed byte -> unsigned int conversion.
 * <p>
 * If the address is not writable it should do nothing and if it's not readable it should return 0xFF.
 */
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
