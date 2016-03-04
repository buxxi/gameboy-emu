package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class DisableInterrupts implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        //TODO: implement me correctly when implemention interrupts
        System.out.println(getClass().getName() + " not implemented");
        return 4;
    }
}
