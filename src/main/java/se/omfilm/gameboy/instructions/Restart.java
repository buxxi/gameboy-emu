package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class Restart implements Instruction {
    private final int address;

    public Restart(int address) {
        this.address = address;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.decreaseWord();
        memory.writeByte(stackPointer.read(), programCounter.read());
        programCounter.write(address);

        return 32;
    }

    public static Instruction to28() {
        return new Restart(0x28);
    }

    public static Instruction to38() { return new Restart(0x38); }
}
