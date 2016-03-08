package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class AddWordRegisterIntoHL implements Instruction {
    private final RegisterReader source;

    public AddWordRegisterIntoHL(RegisterReader source) {
        this.source = source;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int a = registers.readHL();
        int result = n + a;

        boolean carry = result > 0xFFFF;
        result = result & 0xFFFF;
        boolean halfCarry = ((result ^ a ^ n) & 0x1000) != 0;

        registers.writeHL(result);
        flags.set(Flags.flags(false, halfCarry, carry));

        return 4;
    }

    public static Instruction BC() { return new AddWordRegisterIntoHL(Registers::readBC); }

    public static Instruction DE() {
        return new AddWordRegisterIntoHL(Registers::readDE);
    }
}
