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

    private State state = State.NORMAL;

    public CPU(boolean debug) {
        this.instructionProvider = debug ? new DebugPrinter.DebuggableInstructionProvider() : new InstructionProvider();
        for (Instruction.InstructionType type : Instruction.InstructionType.values()) {
            instructionProvider.add(type, type.instruction().get());
        }
        instructionProvider.add(Instruction.InstructionType.STOP, this::stop);
        instructionProvider.add(Instruction.InstructionType.HALT, this::halt);
    }

    private int stop(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        state = State.STOPPED;
        //TODO: make screen go blank during stopped state
        return 4;
    }

    private int halt(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        state = State.HALTED;
        ((InterruptsImpl) interrupts).interruptMasterEnable = true;
        return 4;
    }

    public int step(Memory memory) {
        if (state != State.NORMAL) {
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

        public void writeA(int val) {
            verify(val);
            this.a = val;
        }

        public void writeF(int val) {
            this.f = val & 0xF0;
        }

        public int readB() {
            return this.b;
        }

        public int readC() {
            return this.c;
        }

        public void writeB(int val) {
            verify(val);
            this.b = val;
        }

        public void writeC(int val) {
            verify(val);
            this.c = val;
        }

        public int readD() {
            return this.d;
        }

        public int readE() {
            return this.e;
        }

        public void writeD(int val) {
            verify(val);
            this.d = val;
        }

        public void writeE(int val) {
            verify(val);
            this.e = val;
        }

        public int readH() {
            return this.h;
        }

        public int readL() {
            return this.l;
        }

        public void writeH(int val) {
            verify(val);
            this.h = val;
        }

        public void writeL(int val) {
            verify(val);
            this.l = val;
        }

        private void verify(int val) {
            if (val < 0 || val > 0xFF) {
                throw new IllegalStateException("Can't write value " + DebugPrinter.hex(val, 4) + ", not in range " + DebugPrinter.hex(0, 2) + "-" + DebugPrinter.hex(0xFF, 2));
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
                if (state == State.STOPPED) {
                    state = State.NORMAL;
                }
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
            if (state == State.HALTED) {
                state = State.NORMAL;
                return CPU.this.step(memory);
            }

            interruptMasterEnable = false;
            requestedInterrupts = (requestedInterrupts) & ~(interrupt.mask());
            stackPointer.push(memory, programCounter.read());

            return interrupt.jump(programCounter);
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

    private enum State {
        NORMAL,
        STOPPED,
        HALTED
    }
}
