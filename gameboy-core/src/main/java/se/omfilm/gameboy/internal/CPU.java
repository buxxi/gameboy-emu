package se.omfilm.gameboy.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.Instruction.InstructionType;
import se.omfilm.gameboy.internal.instructions.InvalidInstruction;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.EnumMap;
import java.util.Map;

public class CPU {
    private static final Logger log = LoggerFactory.getLogger(CPU.class);

    public static int FREQUENCY = 4 * 1024 * 1024;

    private final InstructionProvider instructionProvider = new InstructionProvider();
    private final Flags flags = new FlagsImpl();
    private final InterruptsImpl interrupts = new InterruptsImpl();
    private final ProgramCounter programCounter = new ProgramCounterImpl();
    private final StackPointer stackPointer = new StackPointerImpl();
    private final Registers registers = new RegistersImpl();

    private State state = new NormalState();

    @SuppressWarnings("unused")
    private int stop(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        state = new StoppedState();
        log.info("Going into stopped state");
        return 4;
    }

    @SuppressWarnings("unused")
    private int halt(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        if ((interrupts.requestedInterrupts & interrupts.enabledInterrupts) != 0 && !interrupts.interruptMasterEnable) {
            log.warn(InstructionType.HALT + " bug triggered, the program counter wont be incremented for the next instruction");
            state = new HaltBugState();
        } else {
            state = new HaltedState();
        }

        return 4;
    }

    public int step(Memory memory) {
        return state.step(memory) + interrupts.step(memory);
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

    public StackPointer stackPointer() {
        return this.stackPointer;
    }

    public ProgramCounter programCounter() {
        return this.programCounter;
    }

    public Registers registers() {
        return this.registers;
    }

    public Flags flags() {
        return flags;
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
            interrupts.setInterruptsDisabled(disabled);
        }

        public boolean isInterruptsDisabled() {
            return !interrupts.interruptMasterEnable;
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

        private int step(Memory memory) {
            switch (enableDelay) {
                case 2:
                    enableDelay--;
                    break;
                case 1:
                    interruptMasterEnable = true;
                    enableDelay--;
                    break;
            }

            if ((requestedInterrupts & enabledInterrupts) == 0) {
                return 0;
            }

            return state.execute(memory);
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

        private void setInterruptsDisabled(boolean disabled) {
            if (!disabled) {
                enableDelay = 2;
            } else {
                interruptMasterEnable = false;
            }
        }

        private int execute(Interrupt interrupt, Memory memory) {
            interruptMasterEnable = false;
            requestedInterrupts = (requestedInterrupts) & ~(interrupt.mask());
            stackPointer.push(memory, programCounter.read());

            return interrupt.jump(programCounter);
        }
    }

    private class InstructionProvider {
        private final Map<InstructionType, Instruction> instructionMap = new EnumMap<>(InstructionType.class);
        private final Instruction invalidInstruction = new InvalidInstruction();

        public InstructionProvider() {
            for (InstructionType type : InstructionType.values()) {
                add(type, type.instruction().get());
            }
            add(InstructionType.STOP, CPU.this::stop);
            add(InstructionType.HALT, CPU.this::halt);
        }

        public Instruction read(ProgramCounter programCounter, Memory memory) {
            return resolveImpl(resolveType(programCounter, memory));
        }

        public void add(InstructionType type, Instruction impl) {
            instructionMap.put(type, impl);
        }

        protected InstructionType resolveType(ProgramCounter programCounter, Memory memory) {
            InstructionType instructionType = InstructionType.fromOpCode(programCounter.byteOperand(memory));
            if (instructionType == InstructionType.CB) {
                instructionType = InstructionType.fromOpCode(instructionType.opcode(), programCounter.byteOperand(memory));
            }
            return instructionType;
        }

        protected Instruction resolveImpl(InstructionType instructionType) {
            return instructionMap.getOrDefault(instructionType, invalidInstruction);
        }
    }

    /**
     * The CPU can have multiple states that affect the executing of instructions and/or interrupts.
     */
    private interface State {
        int step(Memory memory);

        int execute(Memory memory);
    }

    /**
     * The normal state for the CPU.
     * Execute instructions as normal and service requests only when IME is set.
     */
    private class NormalState implements State {
        Instruction previous = null;

        public int step(Memory memory) {
            Instruction instruction = previous != null ? previous : instructionProvider.read(programCounter, memory);
            int padding = instruction.paddingCycles();

            if (padding > 0 && previous == null) {
                previous = instruction;
                return padding;
            }

            previous = null;

            return instruction.execute(memory, registers, flags, programCounter, stackPointer) - padding;
        }

        public int execute(Memory memory) {
            if (!interrupts.interruptMasterEnable || previous != null) {
                return 0;
            }
            for (Interrupts.Interrupt interrupt : Interrupts.Interrupt.cachedValues()) {
                if ((interrupt.mask() & interrupts.requestedInterrupts & interrupts.enabledInterrupts) != 0) {
                    return interrupts.execute(interrupt, memory);
                }
            }
            return 0;
        }
    }

    /**
     * The state the CPU is in while waiting for input from the user.
     * The screen should go to Shade.LIGHTEST while in this state.
     * Service the interrupt and return to normal when a key has been pressed.
     * It will get stuck in this state if neither buttons or keys has been set as requested.
     */
    private class StoppedState implements State {
        public int step(Memory memory) {
            return 0;
        }

        public int execute(Memory memory) {
            if (interrupts.requested(Interrupts.Interrupt.JOYPAD)) {
                state = new NormalState();
                return state.execute(memory);
            }
            return 0;
        }
    }

    /**
     * The state the CPU is is while waiting for any interrupt to be serviced.
     * Return to normal state when a interrupt is serviced.
     */
    private class HaltedState implements State {
        public int step(Memory memory) {
            return 4;
        }

        public int execute(Memory memory) {
            state = new NormalState();
            return state.execute(memory);
        }
    }

    /**
     * When trying to go into HALT and interrupts is disabled but a valid one is already queued the CPU goes to this state.
     * It executes like normally but afterwards the program counter is restored to what is was before and the state is set to Normal again.
     */
    private class HaltBugState implements State {
        public int step(Memory memory) {
            State normalState = new NormalState();
            int before = programCounter.read();
            int cycles = normalState.step(memory);
            programCounter.write(before);
            state = normalState;
            return cycles;
        }

        public int execute(Memory memory) {
            return 0;
        }
    }
}
