package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class IncrementWordRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public IncrementWordRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int val = (source.read(registers) + 1) % 0xFFFF;
        target.write(registers, val);
    }

    public static Instruction HL() {
        return new IncrementWordRegister(Registers::readHL, Registers::writeHL);
    }

    public static Instruction DE() {
        return new IncrementWordRegister(Registers::readDE, Registers::writeDE);
    }
}
