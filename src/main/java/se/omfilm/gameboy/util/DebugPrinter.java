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
    private static RecordedInstruction currentInstruction;

    public static void debug(StackPointer stackPointer, ProgramCounter programCounter) {
        log.debug(
            "PC:\t" + hex(programCounter.read(), 4) + "\tSP:\t" + hex(stackPointer.read(), 4)
        );
    }

    public static void debug(Registers registers) {
        log.debug(
            "A:\t" + hex(registers.readA(), 2) + "\tF:\t" + hex(registers.readF(), 2) + "\tAF:\t" + hex(registers.readAF(), 4) + "\n" +
            "B:\t" + hex(registers.readB(), 2) + "\tC:\t" + hex(registers.readC(), 2) + "\tBC:\t" + hex(registers.readBC(), 4) + "\n" +
            "H:\t" + hex(registers.readH(), 2) + "\tL:\t" + hex(registers.readL(), 2) + "\tHL:\t" + hex(registers.readHL(), 4) + "\n" +
            "D:\t" + hex(registers.readD(), 2) + "\tD:\t" + hex(registers.readE(), 2) + "\tDE:\t" + hex(registers.readDE(), 4) + "\n"
        );
    }

    public static String hex(int val, int length) {
        String result = Integer.toHexString(val).toUpperCase();
        while (result.length() < length) {
            result = "0" + result;
        }
        return "0x" + result;
    }

    public static void debugException(Exception e) throws InterruptedException {
        Thread.sleep(100);
        e.printStackTrace();
        log.error(Instruction.InstructionType.values().length + " instructions implemented of 512");
        log.error("Instruction call stack: ");
        debugCallStack();
        System.exit(0);
    }

    public static void debugCallStack() {
        LinkedList<RecordedInstruction> copy = (LinkedList<RecordedInstruction>) instructionStack.clone();
        while (!copy.isEmpty()) {
            log.error("\t" + copy.removeLast());
        }
    }

    public static void record(Instruction.InstructionType instructionType, int sourceProgramCounter) {
        currentInstruction = new RecordedInstruction(instructionType, sourceProgramCounter);
        instructionStack.add(currentInstruction);
        while (instructionStack.size() > 32) {
            instructionStack.remove();
        }
    }

    public static Memory record(Memory memory) {
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
                    currentInstruction.modifications.add(
                            "Mem @ " + DebugPrinter.hex(address, 4) + ": " + previousValue + " -> " + hex(data, 2)
                    );
                }
                memory.writeByte(address, data);
            }
        };
    }

    public static Registers record(Registers registers) {
        return new Registers() {
            public int readH() {
                return registers.readH();
            }

            public void writeH(int val) {
                currentInstruction.modifications.add(
                        "H: " + hex(readH(), 2) + " -> " + hex(val, 2)
                );
                registers.writeH(val);
            }

            public int readL() {
                return registers.readL();
            }

            public void writeL(int val) {
                currentInstruction.modifications.add(
                        "L: " + hex(readL(), 2) + " -> " + hex(val, 2)
                );
                registers.writeL(val);
            }

            public int readHL() {
                return registers.readHL();
            }

            public void writeHL(int val) {
                currentInstruction.modifications.add(
                        "HL: " + hex(readHL(), 4) + " -> " + hex(val, 4)
                );
                registers.writeHL(val);
            }

            public int readDE() {
                return registers.readDE();
            }

            public void writeDE(int val) {
                currentInstruction.modifications.add(
                        "DE: " + hex(readDE(), 4) + " -> " + hex(val, 4)
                );
                registers.writeDE(val);
            }

            public int readA() {
                return registers.readA();
            }

            public void writeA(int val) {
                currentInstruction.modifications.add(
                        "A: " + hex(readA(), 2) + " -> " + hex(val, 2)
                );
                registers.writeA(val);
            }

            public int readC() {
                return registers.readC();
            }

            public void writeC(int val) {
                currentInstruction.modifications.add(
                        "C: " + hex(readC(), 2) + " -> " + hex(val, 2)
                );
                registers.writeC(val);
            }

            public int readB() {
                return registers.readB();
            }

            public void writeB(int val) {
                currentInstruction.modifications.add(
                        "B: " + hex(readB(), 2) + " -> " + hex(val, 2)
                );
                registers.writeB(val);
            }

            public int readBC() {
                return registers.readBC();
            }

            public void writeBC(int val) {
                currentInstruction.modifications.add(
                        "BC: " + hex(readBC(), 4) + " -> " + hex(val, 4)
                );
                registers.writeBC(val);
            }

            public int readD() {
                return registers.readD();
            }

            public void writeD(int val) {
                currentInstruction.modifications.add(
                        "D: " + hex(readD(), 2) + " -> " + hex(val, 2)
                );
                registers.writeD(val);
            }

            public int readE() {
                return registers.readE();
            }

            public void writeE(int val) {
                currentInstruction.modifications.add(
                        "E: " + hex(readE(), 2) + " -> " + hex(val, 2)
                );
                registers.writeE(val);
            }

            public int readF() {
                return registers.readF();
            }

            public void writeF(int val) {
                currentInstruction.modifications.add(
                        "F: " + hex(readF(), 2) + " -> " + hex(val, 2)
                );
                registers.writeF(val);
            }

            public int readAF() {
                return registers.readAF();
            }

            public void writeAF(int val) {
                currentInstruction.modifications.add(
                        "AF: " + hex(readAF(), 4) + " -> " + hex(val, 4)
                );
                registers.writeAF(val);
            }
        };
    }

    public static Flags record(Flags flags) {
        return new Flags() {
            public boolean isSet(Flag flag) {
                return flags.isSet(flag);
            }

            public void set(Flag flag, boolean set) {
                if (isSet(flag) != set) {
                    currentInstruction.modifications.add(
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

    public static ProgramCounter record(ProgramCounter programCounter) {
        return new ProgramCounter() {
            public int read() {
                return programCounter.read();
            }

            public void write(int data) {
                currentInstruction.modifications.add(
                    "PC: " + hex(read(), 4) + " -> " + hex(data, 4)
                );
                programCounter.write(data);
            }
        };
    }

    public static StackPointer record(StackPointer stackPointer) {
        return new StackPointer() {
            public void write(int value) {
                currentInstruction.modifications.add(
                    "SP: " + hex(read(), 4) + " -> " + hex(value, 4)
                );
                stackPointer.write(value);
            }

            public int read() {
                return stackPointer.read();
            }
        };
    }

    public static void verifyBoot(CPU cpu, StackPointer stackPointer) {
        verify("AF", 0x01B0, cpu.readAF());
        verify("BC", 0x0013, cpu.readBC());
        verify("DE", 0x00D8, cpu.readDE());
        verify("HL", 0x014D, cpu.readHL());
        verify("SP", 0xFFFE, stackPointer.read());
        instructionStack.clear();
        instructionStack.add(currentInstruction);
    }

    private static void verify(String name, int expected, int got) {
        if (got != expected) {
            throw new IllegalStateException("Register " + name + " has wrong value after boot, expected: " + hex(expected, 4) + " but got: " + hex(got, 4));
        }
    }

    private static class RecordedInstruction {
        private final Instruction.InstructionType instructionType;
        private final int sourceProgramCounter;

        private List<String> modifications = new ArrayList<>();

        public RecordedInstruction(Instruction.InstructionType instructionType, int sourceProgramCounter) {
            this.instructionType = instructionType;
            this.sourceProgramCounter = sourceProgramCounter;
        }

        @Override
        public String toString() {
            return instructionType + " @ " + hex(sourceProgramCounter, 4) + " " + modifications;
        }
    }
}
