package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class AddRegisterWithCarryIntoA implements Instruction {
    private final RegisterReader source;

    private AddRegisterWithCarryIntoA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readA();
        int result = n + a + (flags.isSet(Flags.Flag.CARRY) ? 1 : 0);

        boolean carry = result > 0xFF;
        result = result & 0xFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x10) != 0;
        boolean zero = result == 0;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 4;
    }

    public static Instruction A() {
        return new AddRegisterWithCarryIntoA(Registers::readA);
    }

    public static Instruction B() {
        return new AddRegisterWithCarryIntoA(Registers::readB);
    }

    public static Instruction C() {
        return new AddRegisterWithCarryIntoA(Registers::readC);
    }

    public static Instruction D() {
        return new AddRegisterWithCarryIntoA(Registers::readD);
    }

    public static Instruction E() {
        return new AddRegisterWithCarryIntoA(Registers::readE);
    }

    public static Instruction H() {
        return new AddRegisterWithCarryIntoA(Registers::readH);
    }

    public static Instruction L() {
        return new AddRegisterWithCarryIntoA(Registers::readL);
    }
}
