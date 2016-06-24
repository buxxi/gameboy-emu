package se.omfilm.gameboy.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.EnumMap;
import java.util.Map;

public class CPU {
    private static final Logger log = LoggerFactory.getLogger(CPU.class);

    public static int FREQUENCY = 4 * 1024 * 1024;

    private final InstructionProvider instructionProvider;
    private final Flags flags = new FlagsImpl();
    private final Interrupts interrupts = new InterruptsImpl();
    private final ProgramCounter programCounter = new ProgramCounterImpl();
    private final StackPointer stackPointer = new StackPointerImpl();
    private final Registers registers = new RegistersImpl();

    private boolean halted = false;

    public CPU(boolean debug) {
        this.instructionProvider = debug ? new DebugPrinter.DebuggableInstructionProvider() : new InstructionProvider();
        for (Instruction.InstructionType type : Instruction.InstructionType.values()) {
            instructionProvider.add(type, type.instruction().get());
        }
        instructionProvider.add(Instruction.InstructionType.STOP, this::stop);
        instructionProvider.add(Instruction.InstructionType.HALT, this::halt);
    }

    private int stop(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        log.warn("stop() called but is not implemented yet");
        //TODO: handle me, stop until button is pressed
        return 4;
    }

    private int halt(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        halted = true;
        ((InterruptsImpl) interrupts).interruptMasterEnable = true;
        return 4;
    }

    public int step(Memory memory) {
        if (halted) {
            return 4;
        }

        Instruction instruction = instructionProvider.read(programCounter, memory);
        return instruction.execute(memory, this.registers, this.flags, this.programCounter, this.stackPointer);
    }

    public void reset() {
        programCounter.write(0x100);
        stackPointer.write(0xFFFE);
        registers.writeAF(0x01B0);
        registers.writeBC(0x0013);
        registers.writeDE(0x00D8);
        registers.writeHL(0x014D);
        for (Interrupts.Interrupt interrupt : Interrupts.Interrupt.cachedValues()) {
            interrupts.enable(interrupt, false);
            interrupts.request(interrupt, false);
        }
    }

    public Interrupts interrupts() {
        return this.interrupts;
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
            return (registers.readF() & flag.mask()) != 0;
        }

        public void set(Flag flag, boolean value) {
            int f = registers.readF();
            if (value) {
                f = f | flag.mask();
            } else {
                f = f & (~flag.mask());
            }
            registers.writeF(f);
        }

        public void setInterruptsDisabled(boolean disabled) {
            ((InterruptsImpl) interrupts).setInterruptsDisabled(disabled);
        }
    }

    private class RegistersImpl implements Registers {
        private int a = 0;
        private int f = 0;
        private int b = 0;
        private int c = 0;
        private int h = 0;
        private int l = 0;
        private int d = 0;
        private int e = 0;

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

        private void verify(int val, int maxValue) {
            if (val < 0 || val > maxValue) {
                throw new IllegalStateException("Can't write value " + DebugPrinter.hex(val, 4) + ", not in range " + DebugPrinter.hex(0, 4) + "-" + DebugPrinter.hex(maxValue, 4));
            }
        }
    }

    private class InterruptsImpl implements Interrupts {
        private boolean interruptMasterEnable = false;
        private int enableDelay = 0;
        private int enabledInterrupts = 0;
        private int requestedInterrupts = 0;

        public int step(Memory memory) {
            switch (enableDelay) {
                case 2:
                    enableDelay--;
                    break;
                case 1:
                    interruptMasterEnable = true;
                    enableDelay--;
                    break;
            }

            if ((!interruptMasterEnable) || (requestedInterrupts & enabledInterrupts) == 0) {
                return 0;
            }

            for (Interrupt interrupt : Interrupt.cachedValues()) {
                if ((interrupt.mask() & requestedInterrupts & enabledInterrupts) != 0) {
                    return execute(interrupt, memory);
                }
            }
            return 0;
        }

        private void setInterruptsDisabled(boolean disabled) {
            if (!disabled) {
                enableDelay = 2;
            } else {
                interruptMasterEnable = false;
            }
        }

        public void enable(Interrupt interrupt, boolean enabled) {
            if (enabled) {
                enabledInterrupts = enabledInterrupts | interrupt.mask();
            } else {
                enabledInterrupts = enabledInterrupts & (~interrupt.mask());
            }
        }

        public void request(Interrupt interrupt, boolean requested) {
            if (requested) {
                requestedInterrupts = requestedInterrupts | interrupt.mask();
            } else {
                requestedInterrupts = requestedInterrupts & (~interrupt.mask());
            }
        }

        public boolean enabled(Interrupt interrupt) {
            return (enabledInterrupts & interrupt.mask()) != 0;
        }

        public boolean requested(Interrupt interrupt) {
            return (requestedInterrupts & interrupt.mask()) != 0;
        }

        private int execute(Interrupt interrupt, Memory memory) {
            requestedInterrupts = (requestedInterrupts) & ~(interrupt.mask());
            interruptMasterEnable = false;
            halted = false;
            stackPointer.push(memory, programCounter.read());

            //TODO: move these values to the enum
            switch (interrupt) {
                case VBLANK:
                    programCounter.write(0x40);
                    return 20;
                case TIMER:
                    programCounter.write(0x50);
                    return 20;
                case JOYPAD:
                    programCounter.write(0x60);
                    return 20;
                case LCD:
                    programCounter.write(0x48);
                    return 20;
                default:
                    throw new UnsupportedOperationException("Interrupt " + interrupt + " not implemented");
            }
        }
    }

    public static class InstructionProvider {
        private final Map<Instruction.InstructionType, Instruction> instructionMap = new EnumMap<>(Instruction.InstructionType.class);

        public Instruction read(ProgramCounter programCounter, Memory memory) {
            return resolveImpl(resolveType(programCounter, memory));
        }

        public void add(Instruction.InstructionType type, Instruction impl) {
            instructionMap.put(type, impl);
        }

        protected Instruction.InstructionType resolveType(ProgramCounter programCounter, Memory memory) {
            Instruction.InstructionType instructionType = Instruction.InstructionType.fromOpCode(programCounter.byteOperand(memory));
            if (instructionType == Instruction.InstructionType.CB) {
                instructionType = Instruction.InstructionType.fromOpCode(instructionType.opcode(), programCounter.byteOperand(memory));
            }
            return instructionType;
        }

        protected Instruction resolveImpl(Instruction.InstructionType instructionType) {
            return instructionMap.getOrDefault(instructionType, unmappedInstruction(instructionType));
        }

        private Instruction unmappedInstruction(Instruction.InstructionType instructionType) {
            return (memory, registers, flags, programCounter, stackPointer) -> {
                throw new UnsupportedOperationException(instructionType + " not implemented");
            };
        }
    }
}
