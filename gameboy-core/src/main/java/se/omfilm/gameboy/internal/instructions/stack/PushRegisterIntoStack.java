package se.omfilm.gameboy.internal.instructions.stack;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class PushRegisterIntoStack implements Instruction {
    private final RegisterReader source;

    private PushRegisterIntoStack(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.push(memory, source.read(registers));

        return 16;
    }

    public static Instruction fromAF() {
        return new PushRegisterIntoStack(Registers::readAF);
    }

    public static Instruction fromBC() {
        return new PushRegisterIntoStack(Registers::readBC);
    }

    public static Instruction fromDE() {
        return new PushRegisterIntoStack(Registers::readDE);
    }

    public static Instruction fromHL() {
        return new PushRegisterIntoStack(Registers::readHL);
    }
}
