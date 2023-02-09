package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.Memory;

/**
 * Represents the program counter for the CPU.
 * This keeps track of what code to execute from the ROM.
 * Every time a instruction is parsed it first reads the opcode and steps the program counter one step,
 * and then optionally depending on the instruction steps it even more forward if the instruction needs any operands.
 * <p>
 * When the program counter reaches 0x100 it has successfully booted the boot-rom and 0xFF50 is set to 1 for disabling the boot-rom.
 */
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
