package se.omfilm.gameboy.internal.instructions.load;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

public class LoadByteAddressOfRegisterIntoRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    private LoadByteAddressOfRegisterIntoRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, memory.readByte(source.read(registers)));

        return 8;
    }

    public static Instruction fromBCtoA() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readBC, Registers::writeA);
    }

    public static Instruction fromDEtoA() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readDE, Registers::writeA);
    }

    public static Instruction fromHLtoA() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeA);
    }

    public static Instruction fromHLtoB() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeB);
    }

    public static Instruction fromHLtoC() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeC);
    }

    public static Instruction fromHLtoD() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeD);
    }

    public static Instruction fromHLtoE() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeE);
    }

    public static Instruction fromHLtoH() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeH);
    }

    public static Instruction fromHLtoL() {
        return new LoadByteAddressOfRegisterIntoRegister(Registers::readHL, Registers::writeL);
    }
}
