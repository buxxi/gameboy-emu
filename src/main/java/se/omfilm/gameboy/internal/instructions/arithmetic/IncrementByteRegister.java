package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class IncrementByteRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private IncrementByteRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int result = (n + 1) & 0xFF;
        target.write(registers, result);

        boolean zero = result == 0;
        boolean halfCarry = (n & 0x0F) == 0x0F;

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);

        return 4;
    }

    public static Instruction A() { return new IncrementByteRegister(Registers::readA, Registers::writeA); }

    public static Instruction B() {
        return new IncrementByteRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction C() {
        return new IncrementByteRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new IncrementByteRegister(Registers::readD, Registers::writeD);
    }

    public static Instruction E() {
        return new IncrementByteRegister(Registers::readE, Registers::writeE);
    }

    public static Instruction H() {
        return new IncrementByteRegister(Registers::readH, Registers::writeH);
    }

    public static Instruction L() {
        return new IncrementByteRegister(Registers::readL, Registers::writeL);
    }
}
