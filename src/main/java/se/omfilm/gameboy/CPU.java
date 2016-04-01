package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.*;

import static se.omfilm.gameboy.util.DebugPrinter.record;

public class CPU implements Registers {
    private static final Logger log = LoggerFactory.getLogger(CPU.class);

    public static int FREQUENCY = 4 * 1024 * 1024;

    public FlagsImpl flags = new FlagsImpl();
    public InterruptsImpl interrupts = new InterruptsImpl();
    private ProgramCounter programCounter = new ProgramCounterImpl();
    private StackPointer stackPointer = new StackPointerImpl();

    private int a = 0;
    private int f = 0;
    private int b = 0;
    private int c = 0;
    private int h = 0;
    private int l = 0;
    private int d = 0;
    private int e = 0;


    private final Map<Instruction.InstructionType, Instruction> instructionMap;

    public CPU() {
        instructionMap = new EnumMap<>(Instruction.InstructionType.class);
        for (Instruction.InstructionType type : Instruction.InstructionType.values()) {
            instructionMap.put(type, type.instruction().get());
        }
        instructionMap.put(Instruction.InstructionType.STOP, this::stop);
        instructionMap.put(Instruction.InstructionType.HALT, this::halt);
    }

    private int stop(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        log.warn("stop() called but is not implemented yet");
        //TODO: handle me, stop until button is pressed
        return 4;
    }

    private int halt(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        log.warn("halt() called but is not implemented yet");
        //TODO: handle me, stop until interrupt has occured
        return 4;
    }

    public int step(MMU memory) {

        if (programCounter.read() == 0x100) {
            memory.bootSuccess();
            DebugPrinter.verifyBoot(this, this.stackPointer);
        }
        int sourceProgramCounter = programCounter.read();
        Instruction.InstructionType instructionType = Instruction.InstructionType.fromOpCode(programCounter.byteOperand(memory));
        if (instructionType == Instruction.InstructionType.CB) {
            instructionType = Instruction.InstructionType.fromOpCode(instructionType.opcode(), programCounter.byteOperand(memory));
        }

        Instruction instruction = instructionMap.getOrDefault(instructionType, unmappedInstruction(instructionType));
        DebugPrinter.record(instructionType, sourceProgramCounter);

        return instruction.execute(record(memory), record(this), record(this.flags), record(this.programCounter), record(this.stackPointer));
    }

    public void interruptStep(MMU memory) {
        this.interrupts.step(memory);
    }

    private Instruction unmappedInstruction(Instruction.InstructionType instructionType) {
        return (memory, registers, flags, programCounter, stackPointer) -> {
            throw new UnsupportedOperationException(instructionType + " not implemented");
        };
    }

    public int readH() {
        return this.h;
    }

    public int readL() {
        return this.l;
    }

    public int readHL() {
        return this.h << 8 | this.l;
    }

    public void writeH(int val) {
        verify(val, 0xFF);
        this.h = val;
    }

    public void writeL(int val) {
        verify(val, 0xFF);
        this.l = val;
    }

    public void writeHL(int val) {
        verify(val, 0xFFFF);
        writeH(val >> 8);
        writeL(val & 0x00FF);
    }

    public int readD() {
        return this.d;
    }

    public int readE() {
        return this.e;
    }

    public int readDE() {
        return this.d << 8 | this.e;
    }

    public void writeD(int val) {
        verify(val, 0xFF);
        this.d = val;
    }

    public void writeE(int val) {
        verify(val, 0xFF);
        this.e = val;
    }

    public void writeDE(int val) {
        verify(val, 0xFFFF);
        writeD(val >> 8);
        writeE(val & 0x00FF);
    }

    public int readA() {
        return this.a;
    }

    public int readF() {
        return this.f;
    }

    public int readAF() {
        return this.a << 8 | this.f;
    }

    public void writeA(int val) {
        verify(val, 0xFF);
        this.a = val;
    }

    public void writeF(int val) {
        this.f = val & 0xF0;
    }

    public void writeAF(int val) {
        verify(val, 0xFFFF);
        writeA(val >> 8);
        writeF(val & 0x00FF);
    }

    public int readB() {
        return this.b;
    }

    public int readC() {
        return this.c;
    }

    public int readBC() {
        return this.b << 8 | this.c;
    }

    public void writeB(int val) {
        verify(val, 0xFF);
        this.b = val;
    }

    public void writeC(int val) {
        verify(val, 0xFF);
        this.c = val;
    }

    public void writeBC(int val) {
        verify(val, 0xFFFF);
        writeB(val >> 8);
        writeC(val & 0x00FF);
    }

    private void verify(int val, int maxValue) {
        if (val < 0 || val > maxValue) {
            throw new IllegalStateException("Can't write value " + DebugPrinter.hex(val, 4) + ", not in range " + DebugPrinter.hex(0, 4) + "-" + DebugPrinter.hex(maxValue, 4));
        }
    }

    private static class ProgramCounterImpl implements ProgramCounter {
        private int value = 0;

        public int read() {
            return value;
        }

        public void write(int data) {
            this.value = data & 0xFFFF;
        }
    }

    private static class StackPointerImpl implements StackPointer {
        private int value = 0;

        public void write(int value) {
            this.value = value;
        }

        public int read() {
            return value;
        }
    }

    private class FlagsImpl implements Flags {
        public boolean isSet(Flag flag) {
            return (readF() & flag.mask) != 0;
        }

        public void set(Flag flag, boolean value) {
            int f = readF();
            if (value) {
                f = f | flag.mask;
            } else {
                f = f & (~flag.mask);
            }
            writeF(f);
        }

        public void setInterruptsDisabled(boolean disabled) {
            interrupts.interruptsDisabled = disabled;
        }
    }

    private class InterruptsImpl implements Interrupts {
        private boolean interruptsDisabled = false;
        private int enabledInterrupts = 0;
        private int requestedInterrupts = 0;

        private void step(MMU memory) {
            if (interruptsDisabled || (requestedInterrupts & enabledInterrupts) == 0) {
                return;
            }

            for (Interrupt interrupt : Interrupt.values()) {
                if ((interrupt.mask & requestedInterrupts & enabledInterrupts) != 0) {
                    execute(interrupt, memory);
                }
            }
        }

        public void enable(Interrupt... interrupts) {
            for (Interrupt i : interrupts) {
                enabledInterrupts = enabledInterrupts & i.mask;
            }
        }

        public void request(Interrupt... interrupts) {
            for (Interrupt i : interrupts) {
                requestedInterrupts = requestedInterrupts & i.mask;
            }
        }

        public boolean enabled(Interrupt interrupt) {
            return (enabledInterrupts & interrupt.mask) != 0;
        }

        public boolean requested(Interrupt interrupt) {
            return (requestedInterrupts & interrupt.mask) != 0;
        }

        private void execute(Interrupt interrupt, MMU memory) {
            requestedInterrupts = (requestedInterrupts) & ~(interrupt.mask);

            stackPointer.push(memory, programCounter.read());

            switch (interrupt) {
                case VBLANK:
                    programCounter.write(0x40);
                    return;
                case TIMER:
                    programCounter.write(0x50);
                    return;
                default:
                    throw new UnsupportedOperationException("Interrupt " + interrupt + " not implemented");
            }
        }
    }
}
