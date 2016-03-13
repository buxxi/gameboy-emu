package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ResetBitInRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;
    private final int bit;

    public ResetBitInRegister(RegisterReader source, RegisterWriter target, int bit) {
        this.source = source;
        this.target = target;
        this.bit = bit;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int value = source.read(registers);
        target.write(registers, value & (~(1 << bit)));
        return 8;
    }

    public static Instruction bit0InA() {
        return new ResetBitInRegister(Registers::readA, Registers::writeA, 0);
    }
}
