package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class RotateRegisterRight implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public RotateRegisterRight(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int addOldCarry = flags.isSet(Flags.Flag.CARRY) ? 0b1000_0000 : 0;
        int n = source.read(registers);

        int result = ((n >> 1) + addOldCarry) & 0xFF;

        boolean zero = result == 0;
        boolean carry = (n & 0b0000_0001) != 0;

        target.write(registers, result);

        flags.set(Flags.Flag.ZERO, zero);
        flags.set(Flags.Flag.SUBTRACT, false);
        flags.set(Flags.Flag.HALF_CARRY, false);
        flags.set(Flags.Flag.CARRY, carry);

        return 8;
    }

    public static Instruction A() {
        return new RotateRegisterRight(Registers::readA, Registers::writeA) {
            public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
                super.execute(memory, registers, flags, programCounter, stackPointer);
                return 4; //TODO: fix this having 4 cycles without anonymous class
            }
        };
    }

    public static Instruction C() {
        return new RotateRegisterRight(Registers::readC, Registers::writeC);
    }

    public static Instruction D() {
        return new RotateRegisterRight(Registers::readD, Registers::writeD);
    }

    public static Instruction E() { return new RotateRegisterRight(Registers::readE, Registers::writeE); }
}
