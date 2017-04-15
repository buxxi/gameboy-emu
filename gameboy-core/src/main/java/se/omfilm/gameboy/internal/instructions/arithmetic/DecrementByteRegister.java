package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class DecrementByteRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private DecrementByteRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int value = source.read(registers);
        boolean halfCarry = (value & 0x0F) == 0;
        value = (value - 1) & 0xFF;
        boolean zero = value == 0;

        target.write(registers, value);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);

        return 4;
    }

    public static Instruction A() { return new DecrementByteRegister(Registers::readA, Registers::writeA); }

    public static Instruction B() {
        return new DecrementByteRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction C() {
        return new DecrementByteRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new DecrementByteRegister(Registers::readD, Registers::writeD);
    }

    public static Instruction E() {
        return new DecrementByteRegister(Registers::readE, Registers::writeE);
    }

    public static Instruction L() {
        return new DecrementByteRegister(Registers::readL, Registers::writeL);
    }

    public static Instruction H() {
        return new DecrementByteRegister(Registers::readH, Registers::writeH);
    }
}
