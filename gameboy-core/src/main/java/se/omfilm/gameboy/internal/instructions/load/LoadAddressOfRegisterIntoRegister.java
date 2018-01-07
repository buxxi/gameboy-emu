package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.instructions.MemoryReadInstruction;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadAddressOfRegisterIntoRegister implements MemoryReadInstruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private LoadAddressOfRegisterIntoRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, memory.readByte(source.read(registers)));

        return totalCycles();
    }

    public int totalCycles() {
        return 8;
    }

    public static Instruction fromBCtoA() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readBC, Registers::writeA);
    }

    public static Instruction fromDEtoA() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readDE, Registers::writeA);
    }

    public static Instruction fromHLtoA() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeA);
    }

    public static Instruction fromHLtoB() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeB);
    }

    public static Instruction fromHLtoC() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeC);
    }

    public static Instruction fromHLtoD() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeD);
    }

    public static Instruction fromHLtoE() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeE);
    }

    public static Instruction fromHLtoH() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeH);
    }

    public static Instruction fromHLtoL() {
        return new LoadAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeL);
    }
}
