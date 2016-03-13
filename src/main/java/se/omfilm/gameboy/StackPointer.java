package se.omfilm.gameboy;

public interface StackPointer {
    void write(int value);

    int read();

    default int pop(Memory memory) {
        int value = memory.readWord(read());
        write(read() + 2);
        return value;
    }

    default void push(Memory memory, int value) {
        int address = read() - 2;
        write(address);
        memory.writeWord(address, value);
    }
}
