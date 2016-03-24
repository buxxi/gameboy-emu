package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class SubtractAddressOfHLFromA implements Instruction {
    private final boolean addCarry;

    private SubtractAddressOfHLFromA(boolean addCarry) {
        this.addCarry = addCarry;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = memory.readByte(registers.readHL());
        int a = registers.readA();

        int result = (a - n - carry(flags)) & 0xFF;
        boolean zero = result == 0;
        boolean carry = n > a;
        boolean halfCarry = (n & 0x0F) > (a & 0x0F);
        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, true);
        flags.set(Flags.Flag.HALF_CARRY, halfCarry);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    private int carry(Flags flags) {
        if (addCarry && flags.isSet(Flags.Flag.CARRY)) {
            return 1;
        }
        return 0;
    }

    public static Instruction withCarry() {
        return new SubtractAddressOfHLFromA(true);
    }

    public static Instruction withoutCarry() {
        return new SubtractAddressOfHLFromA(false);
    }
}
