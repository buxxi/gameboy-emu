package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class SubtractByteFromA implements Instruction {
    private final boolean withCarry;

    private SubtractByteFromA(boolean withCarry) {
        this.withCarry = withCarry;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = programCounter.byteOperand(memory);
        int a = registers.readA();

        int result = (a - n - carry(flags));
        boolean zero = (result & 0xFF) == 0;
        boolean carry = result < 0;
        boolean halfCarry = ((a & 0x0F) - (n & 0x0F) - carry(flags)) < 0;

        registers.writeA(result & 0xFF);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8; //TODO: not in documentation, is this correct?
    }

    private int carry(Flags flags) {
        return withCarry && flags.isSet(Flags.Flag.CARRY) ? 1 : 0;
    }

    public static Instruction withCarry() {
        return new SubtractByteFromA(true);
    }

    public static Instruction withoutCarry() {
        return new SubtractByteFromA(false);
    }
}
