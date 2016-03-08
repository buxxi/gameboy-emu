package se.omfilm.gameboy;

public interface DelayedInstruction extends Instruction {
    boolean disableInterrupts();
}
