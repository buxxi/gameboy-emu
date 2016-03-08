package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AndRegisterWithA implements Instruction {
    private final RegisterReader source;

    public AndRegisterWithA(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = source.read(registers);
        int result = a & n;

        registers.writeA(result);
        flags.set(Flags.flags(result == 0, true, false));

        return 4;
    }


    public static Instruction C() {
        return new AndRegisterWithA(Registers::readC);
    }
}
