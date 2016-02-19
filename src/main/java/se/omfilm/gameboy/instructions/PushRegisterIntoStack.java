package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

import java.util.function.Function;

public class PushRegisterIntoStack implements Instruction {
    private Function<Registers, Integer> apply;

    public PushRegisterIntoStack(Function<Registers, Integer> apply) {
        this.apply = apply;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(stackPointer.read() - 2);
        memory.writeWord(stackPointer.read(), apply.apply(registers));
    }

    public static Instruction fromBC() {
        return new PushRegisterIntoStack(Registers::readBC);
    }
}
