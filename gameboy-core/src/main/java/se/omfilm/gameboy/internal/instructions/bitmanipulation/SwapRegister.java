package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class SwapRegister implements Instruction {
    private final Instruction.RegisterReader source;
    private final Instruction.RegisterWriter target;

    private SwapRegister(Instruction.RegisterReader source, Instruction.RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int val = source.read(registers);
        int result = ((val & 0b0000_1111) << 4) | ((val & 0b1111_0000) >> 4);
        target.write(registers, result);

        boolean zero = result == 0;

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, false);

        return 8;
    }

    public static Instruction A() {
        return new SwapRegister(Registers::readA, Registers::writeA);
    }

    public static Instruction B() {
        return new SwapRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction C() {
        return new SwapRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new SwapRegister(Registers::readD, Registers::writeD);
    }

    public static Instruction E() {
        return new SwapRegister(Registers::readE, Registers::writeE);
    }

    public static Instruction H() {
        return new SwapRegister(Registers::readH, Registers::writeH);
    }

    public static Instruction L() {
        return new SwapRegister(Registers::readL, Registers::writeL);
    }
}
