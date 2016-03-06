package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class OrWithA implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public OrWithA(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();
        int n = source.read(registers);
        int result = a | n;

        registers.writeA(result);
        flags.set(Flags.flags(result == 0, false, false));

        return 4;
    }

    public static Instruction C() {
        return new OrWithA(Registers::readC, Registers::writeC);
    }
}
