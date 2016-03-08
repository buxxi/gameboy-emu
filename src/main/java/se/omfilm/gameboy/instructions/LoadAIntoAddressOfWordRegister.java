package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadAIntoAddressOfWordRegister implements Instruction {
    private final RegisterReader source;

    public LoadAIntoAddressOfWordRegister(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        memory.writeByte(source.read(registers), registers.readA());

        return 8;
    }

    public static Instruction HL() {
        return new LoadAIntoAddressOfWordRegister(Registers::readHL);
    }

    public static Instruction BC() {
        return new LoadAIntoAddressOfWordRegister(Registers::readBC);
    }
}
