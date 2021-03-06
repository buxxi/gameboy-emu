package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class AddWordRegisterIntoRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterReader targetReader;
    private final RegisterWriter targetWriter;

    private AddWordRegisterIntoRegister(RegisterReader source, RegisterReader targetReader, RegisterWriter targetWriter) {
        this.source = source;
        this.targetReader = targetReader;
        this.targetWriter = targetWriter;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = targetReader.read(registers);
        int result = n + a;

        boolean carry = result > 0xFFFF;
        result = result & 0xFFFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x1000) != 0;

        targetWriter.write(registers, result);

        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    public static Instruction BCtoHL() {
        return new AddWordRegisterIntoRegister(Registers::readBC, Registers::readHL, Registers::writeHL);
    }

    public static Instruction DEtoHL() {
        return new AddWordRegisterIntoRegister(Registers::readDE, Registers::readHL, Registers::writeHL);
    }

    public static Instruction HLtoHL() {
        return new AddWordRegisterIntoRegister(Registers::readHL, Registers::readHL, Registers::writeHL);
    }
}
