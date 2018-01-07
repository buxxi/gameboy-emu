package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryWriteInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadRegisterIntoAddressOfWordRegister implements MemoryWriteInstruction {
    private final RegisterReader source;
    private final RegisterReader value;

    private LoadRegisterIntoAddressOfWordRegister(RegisterReader source, RegisterReader value) {
        this.source = source;
        this.value = value;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        memory.writeByte(source.read(registers), value.read(registers));

        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }

    public static Instruction AtoBC() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readBC, Registers::readA);
    }

    public static Instruction AtoDE() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readDE, Registers::readA);
    }

    public static Instruction AtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readA);
    }

    public static Instruction BtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readB);
    }

    public static Instruction CtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readC);
    }

    public static Instruction DtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readD);
    }

    public static Instruction EtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readE);
    }

    public static Instruction HtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readH);
    }

    public static Instruction LtoHL() {
        return new LoadRegisterIntoAddressOfWordRegister(Registers::readHL, Registers::readL);
    }
}
