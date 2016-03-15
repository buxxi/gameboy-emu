package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class OrRegisterWithA implements Instruction {
    private final RegisterReader source;

    public OrRegisterWithA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = source.read(registers);
        int result = a | n;

        registers.writeA(result);

        flags.set(Flags.Flag.ZERO, result == 0);
        flags.reset(Flags.Flag.SUBTRACT, Flags.Flag.HALF_CARRY, Flags.Flag.CARRY);

        return 4;
    }

    public static Instruction A() {
        return new OrRegisterWithA(Registers::readA);
    }

    public static Instruction B() {
        return new OrRegisterWithA(Registers::readB);
    }

    public static Instruction C() {
        return new OrRegisterWithA(Registers::readC);
    }
}
