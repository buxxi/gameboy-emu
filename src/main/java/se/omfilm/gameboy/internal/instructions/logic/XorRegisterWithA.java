package se.omfilm.gameboy.internal.instructions.logic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class XorRegisterWithA implements Instruction {
    private final RegisterReader source;

    private XorRegisterWithA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = source.read(registers);
        int result = (a ^ n) & 0xFF;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, false);

        return 4;
    }

    public static Instruction A() {
        return new XorRegisterWithA(Registers::readA);
    }

    public static Instruction B() {
        return new XorRegisterWithA(Registers::readB);
    }

    public static Instruction C() {
        return new XorRegisterWithA(Registers::readC);
    }

    public static Instruction D() {
        return new XorRegisterWithA(Registers::readD);
    }

    public static Instruction E() {
        return new XorRegisterWithA(Registers::readE);
    }

    public static Instruction H() {
        return new XorRegisterWithA(Registers::readH);
    }

    public static Instruction L() {
        return new XorRegisterWithA(Registers::readL);
    }
}
