package se.omfilm.gameboy.internal.instructions.bitmanipulation;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class ShiftRegisterRight implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final int oldMask;

    private ShiftRegisterRight(RegisterReader source, RegisterWriter target, int oldMask) {
        this.source = source;
        this.target = target;
        this.oldMask = oldMask;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int result = ((n >> 1) | (n & oldMask)) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

        target.write(registers, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    private static int keepBit7Mask() {
        return 0b1000_0000;
    }

    private static int resetBit7Mask() {
        return 0b0000_0000;
    }

    public static Instruction A() {
        return new ShiftRegisterRight(Registers::readA, Registers::writeA, resetBit7Mask());
    }

    public static Instruction B() {
        return new ShiftRegisterRight(Registers::readB, Registers::writeB, resetBit7Mask());
    }

    public static Instruction C() {
        return new ShiftRegisterRight(Registers::readC, Registers::writeC, resetBit7Mask());
    }

    public static Instruction D() {
        return new ShiftRegisterRight(Registers::readD, Registers::writeD, resetBit7Mask());
    }

    public static Instruction E() {
        return new ShiftRegisterRight(Registers::readE, Registers::writeE, resetBit7Mask());
    }

    public static Instruction H() {
        return new ShiftRegisterRight(Registers::readH, Registers::writeH, resetBit7Mask());
    }

    public static Instruction L() {
        return new ShiftRegisterRight(Registers::readL, Registers::writeL, resetBit7Mask());
    }

    public static Instruction AkeepBit7() {
        return new ShiftRegisterRight(Registers::readA, Registers::writeA, keepBit7Mask());
    }

    public static Instruction BkeepBit7() {
        return new ShiftRegisterRight(Registers::readB, Registers::writeB, keepBit7Mask());
    }

    public static Instruction CkeepBit7() {
        return new ShiftRegisterRight(Registers::readC, Registers::writeC, keepBit7Mask());
    }

    public static Instruction DkeepBit7() {
        return new ShiftRegisterRight(Registers::readD, Registers::writeD, keepBit7Mask());
    }

    public static Instruction EkeepBit7() {
        return new ShiftRegisterRight(Registers::readE, Registers::writeE, keepBit7Mask());
    }

    public static Instruction HkeepBit7() {
        return new ShiftRegisterRight(Registers::readH, Registers::writeH, keepBit7Mask());
    }

    public static Instruction LkeepBit7() {
        return new ShiftRegisterRight(Registers::readL, Registers::writeL, keepBit7Mask());
    }
}
