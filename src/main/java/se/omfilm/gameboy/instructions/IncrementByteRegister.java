package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class IncrementByteRegister implements Instruction {
    private final RegisterReader source;
    private final RegisterWriter target;

    public IncrementByteRegister(RegisterReader source, RegisterWriter target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int n = source.read(registers);
        int result = (n + 1) & 0xFF;
        target.write(registers, result);

        boolean zero = result == 0;
        boolean halfCarry = (n & 0x0F) == 0x0F;

        flags.set(Flags.flags(zero, halfCarry, false));
        return 4;
    }

    public static Instruction C() {
        return new IncrementByteRegister(Registers::readC, Registers::writeC);
    }

    public static Instruction B() {
        return new IncrementByteRegister(Registers::readB, Registers::writeB);
    }

    public static Instruction H() {
        return new IncrementByteRegister(Registers::readH, Registers::writeH);
    }
}
