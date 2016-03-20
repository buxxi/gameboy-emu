package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class LoadRegisterIntoRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public LoadRegisterIntoRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        target.write(registers, source.read(registers));

        return 4;
    }

    public static Instruction fromAtoA() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeA);
    }

    public static Instruction fromAtoB() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeB);
    }

    public static Instruction fromAtoC() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeC);
    }

    public static Instruction fromAtoD() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeD);
    }

    public static Instruction fromAtoE() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeE);
    }

    public static Instruction fromAtoH() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeH);
    }

    public static Instruction fromAtoL() {
        return new LoadRegisterIntoRegister(Registers::readA, Registers::writeL);
    }


    public static Instruction fromBtoA() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeA);
    }

    public static Instruction fromBtoB() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction fromBtoC() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeC);
    }

    public static Instruction fromBtoD() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeD);
    }

    public static Instruction fromBtoE() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeE);
    }

    public static Instruction fromBtoH() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeH);
    }

    public static Instruction fromBtoL() {
        return new LoadRegisterIntoRegister(Registers::readB, Registers::writeL);
    }


    public static Instruction fromCtoA() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeA);
    }

    public static Instruction fromCtoB() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeB);
    }

    public static Instruction fromCtoC() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction fromCtoD() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeD);
    }

    public static Instruction fromCtoE() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeE);
    }

    public static Instruction fromCtoH() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeH);
    }

    public static Instruction fromCtoL() {
        return new LoadRegisterIntoRegister(Registers::readC, Registers::writeL);
    }


    public static Instruction fromDtoA() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeA);
    }

    public static Instruction fromDtoB() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeB);
    }

    public static Instruction fromDtoC() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeC);
    }

    public static Instruction fromDtoD() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeD);
    }

    public static Instruction fromDtoE() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeE);
    }

    public static Instruction fromDtoH() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeH);
    }

    public static Instruction fromDtoL() {
        return new LoadRegisterIntoRegister(Registers::readD, Registers::writeL);
    }


    public static Instruction fromEtoA() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeA);
    }

    public static Instruction fromEtoB() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeB);
    }

    public static Instruction fromEtoC() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeC);
    }

    public static Instruction fromEtoD() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeD);
    }

    public static Instruction fromEtoE() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeE);
    }

    public static Instruction fromEtoH() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeH);
    }

    public static Instruction fromEtoL() {
        return new LoadRegisterIntoRegister(Registers::readE, Registers::writeL);
    }



    public static Instruction fromHtoA() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeA);
    }

    public static Instruction fromHtoB() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeB);
    }

    public static Instruction fromHtoC() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeC);
    }

    public static Instruction fromHtoD() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeD);
    }

    public static Instruction fromHtoE() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeE);
    }

    public static Instruction fromHtoH() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeH);
    }

    public static Instruction fromHtoL() {
        return new LoadRegisterIntoRegister(Registers::readH, Registers::writeL);
    }


    public static Instruction fromLtoA() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeA);
    }

    public static Instruction fromLtoB() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeB);
    }

    public static Instruction fromLtoC() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeC);
    }

    public static Instruction fromLtoD() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeD);
    }

    public static Instruction fromLtoE() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeE);
    }

    public static Instruction fromLtoH() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeH);
    }

    public static Instruction fromLtoL() {
        return new LoadRegisterIntoRegister(Registers::readL, Registers::writeL);
    }
}
