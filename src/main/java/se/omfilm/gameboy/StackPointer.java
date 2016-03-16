package se.omfilm.gameboy;

public interface StackPointer {
    void write(int value);

    int read();

    default int pop(Memory memory) {
        int address = read();
        int value = (memory.readByte(address + 1) << 8) | memory.readByte(address);
        write(address + 2);
        return value;
    }

    default void push(Memory memory, int value) {
        int address = read() - 1;
        memory.writeByte(address, value >> 8);
        address--;
        memory.writeByte(address, value & 0xFF);
        write(address);
    }
}
