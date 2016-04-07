package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.Memory;

public interface ProgramCounter {
    int read();

    void write(int data);

    default int byteOperand(Memory memory) {
        int pc = read();
        int result = memory.readByte(pc);
        write(pc + 1);
        return result;
    }

    default int wordOperand(Memory memory) {
        int pc = read();
        int result = memory.readWord(pc);
        write(pc + 2);
        return result;
    }
}
