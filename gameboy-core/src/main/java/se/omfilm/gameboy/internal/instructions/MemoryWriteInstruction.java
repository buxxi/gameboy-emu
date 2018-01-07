package se.omfilm.gameboy.internal.instructions;

import se.omfilm.gameboy.internal.Instruction;

/**
 * Instructions that writes to memory should do that in the last machine cycle (the last 4 clock cycles)
 * Make those instructions declare the totalCycles that it uses so the padding can be calculated.
 */
public interface MemoryWriteInstruction extends Instruction {
    /**
     * The amount of clock cycles that should be stepped before executing this instruction
     */
    default int paddingCycles() {
        return totalCycles() - 4;
    }

    /**
     * The total amount of clock cycles the instruction uses, the execute-method should return this value
     */
    int totalCycles();
}
