package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class PushRegisterIntoStack implements Instruction {
    private RegisterReader source;

    public PushRegisterIntoStack(RegisterReader source) {
        this.source = source;
    }

    @Override
    public void execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.write(stackPointer.read() - 2);
        memory.writeWord(stackPointer.read(), source.read(registers));
    }

    public static Instruction fromBC() {
        return new PushRegisterIntoStack(Registers::readBC);
    }
}
