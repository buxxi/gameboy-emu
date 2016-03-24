package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class Restart implements Instruction {
    private final int address;

    public Restart(int address) {
        this.address = address;
    }

    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        stackPointer.push(memory, programCounter.read());
        programCounter.write(address);

        return 32;
    }

    public static Instruction to00() {
        return new Restart(0x00);
    }

    public static Instruction to08() {
        return new Restart(0x08);
    }

    public static Instruction to10() {
        return new Restart(0x10);
    }

    public static Instruction to18() {
        return new Restart(0x18);
    }

    public static Instruction to20() {
        return new Restart(0x20);
    }

    public static Instruction to28() {
        return new Restart(0x28);
    }

    public static Instruction to30() {
        return new Restart(0x30);
    }

    public static Instruction to38() { return new Restart(0x38); }
}
