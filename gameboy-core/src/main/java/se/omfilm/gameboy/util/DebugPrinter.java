package se.omfilm.gameboy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DebugPrinter {
    private static final Logger log = LoggerFactory.getLogger(DebugPrinter.class);

    private static LinkedList<RecordedInstruction> instructionStack = new LinkedList<>();

    public static String hex(int val, int length) {
        String result = Integer.toHexString(val).toUpperCase();
        while (result.length() < length) {
            result = "0" + result;
        }
        return "0x" + result;
    }

    public static void debugException(Exception e) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        e.printStackTrace();
        log.error("Instruction call stack: ");
        debugCallStack();
        System.exit(0);
    }

    private static void debugCallStack() {
        LinkedList<RecordedInstruction> copy = (LinkedList<RecordedInstruction>) instructionStack.clone();
        while (!copy.isEmpty()) {
            log.error("\t" + copy.removeLast());
        }
    }

    public static class DebuggableInstructionProvider extends CPU.InstructionProvider {
        @Override
        public Instruction read(ProgramCounter programCounter, Memory memory) {
            int sourceProgramCounter = programCounter.read();
            Instruction.InstructionType type = resolveType(programCounter, memory);
            RecordedInstruction result = new RecordedInstruction(type, sourceProgramCounter, resolveImpl(type));
            instructionStack.add(result);
            while (instructionStack.size() > 32) {
                instructionStack.remove();
            }
            return result;
        }
    }

    private static class RecordedInstruction implements Instruction {
        private final Instruction.InstructionType instructionType;
        private final int sourceProgramCounter;
        private final Instruction delegate;

        private List<String> modifications = new ArrayList<>();

        public RecordedInstruction(InstructionType instructionType, int sourceProgramCounter, Instruction delegate) {
            this.instructionType = instructionType;
            this.sourceProgramCounter = sourceProgramCounter;
            this.delegate = delegate;
        }

        public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
            return delegate.execute(record(memory), record(registers), record(flags), record(programCounter), record(stackPointer));
        }

        public Memory record(Memory memory) {
            return new Memory() {
                public int readByte(int address) {
                    return memory.readByte(address);
                }

                public void writeByte(int address, int data) {
                    String previousValue = "0x??";
                    boolean add = true;
                    try {
                        int oldValue = readByte(address);
                        previousValue = hex(oldValue, 2);
                        add = oldValue != data;
                    } catch (Exception ignored) {}
                    if (add) {
                        modifications.add(
                                "Mem @ " + DebugPrinter.hex(address, 4) + ": " + previousValue + " -> " + hex(data, 2)
                        );
                    }
                    memory.writeByte(address, data);
                }
            };
        }

        public Registers record(Registers registers) {
            return new Registers() {
                public int readH() {
                    return registers.readH();
                }

                public void writeH(int val) {
                    modifications.add(
                            "H: " + hex(readH(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeH(val);
                }

                public int readL() {
                    return registers.readL();
                }

                public void writeL(int val) {
                    modifications.add(
                            "L: " + hex(readL(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeL(val);
                }

                public int readHL() {
                    return registers.readHL();
                }

                public void writeHL(int val) {
                    modifications.add(
                            "HL: " + hex(readHL(), 4) + " -> " + hex(val, 4)
                    );
                    registers.writeHL(val);
                }

                public int readDE() {
                    return registers.readDE();
                }

                public void writeDE(int val) {
                    modifications.add(
                            "DE: " + hex(readDE(), 4) + " -> " + hex(val, 4)
                    );
                    registers.writeDE(val);
                }

                public int readA() {
                    return registers.readA();
                }

                public void writeA(int val) {
                    modifications.add(
                            "A: " + hex(readA(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeA(val);
                }

                public int readC() {
                    return registers.readC();
                }

                public void writeC(int val) {
                    modifications.add(
                            "C: " + hex(readC(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeC(val);
                }

                public int readB() {
                    return registers.readB();
                }

                public void writeB(int val) {
                    modifications.add(
                            "B: " + hex(readB(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeB(val);
                }

                public int readBC() {
                    return registers.readBC();
                }

                public void writeBC(int val) {
                    modifications.add(
                            "BC: " + hex(readBC(), 4) + " -> " + hex(val, 4)
                    );
                    registers.writeBC(val);
                }

                public int readD() {
                    return registers.readD();
                }

                public void writeD(int val) {
                    modifications.add(
                            "D: " + hex(readD(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeD(val);
                }

                public int readE() {
                    return registers.readE();
                }

                public void writeE(int val) {
                    modifications.add(
                            "E: " + hex(readE(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeE(val);
                }

                public int readF() {
                    return registers.readF();
                }

                public void writeF(int val) {
                    modifications.add(
                            "F: " + hex(readF(), 2) + " -> " + hex(val, 2)
                    );
                    registers.writeF(val);
                }

                public int readAF() {
                    return registers.readAF();
                }

                public void writeAF(int val) {
                    modifications.add(
                            "AF: " + hex(readAF(), 4) + " -> " + hex(val, 4)
                    );
                    registers.writeAF(val);
                }
            };
        }

        public Flags record(Flags flags) {
            return new Flags() {
                public boolean isSet(Flag flag) {
                    return flags.isSet(flag);
                }

                public void set(Flag flag, boolean set) {
                    if (isSet(flag) != set) {
                        modifications.add(
                                "Flag " + flag + ": " + !set + " -> " + set
                        );
                    }
                    flags.set(flag, set);
                }

                public void setInterruptsDisabled(boolean disabled) {
                    flags.setInterruptsDisabled(disabled);
                }
            };
        }

        public ProgramCounter record(ProgramCounter programCounter) {
            return new ProgramCounter() {
                public int read() {
                    return programCounter.read();
                }

                public void write(int data) {
                    modifications.add(
                            "PC: " + hex(read(), 4) + " -> " + hex(data, 4)
                    );
                    programCounter.write(data);
                }
            };
        }

        public StackPointer record(StackPointer stackPointer) {
            return new StackPointer() {
                public void write(int value) {
                    modifications.add(
                            "SP: " + hex(read(), 4) + " -> " + hex(value, 4)
                    );
                    stackPointer.write(value);
                }

                public int read() {
                    return stackPointer.read();
                }
            };
        }

        @Override
        public String toString() {
            return instructionType + " @ " + hex(sourceProgramCounter, 4) + " " + modifications;
        }
    }
}
