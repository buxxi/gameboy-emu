package se.omfilm.gameboy.internal.memory;

import se.omfilm.gameboy.internal.Instruction;
import se.omfilm.gameboy.util.DebugPrinter;

public interface Memory {
    int readByte(int address);

    void writeByte(int address, int data);

    default int readWord(int address) {
        return readByte(address) + ((readByte(address + 1) << 8));
    }

    default void writeWord(int address, int data) {
        writeByte(address, data & 0xFF);
        writeByte(address + 1, data >> 8);
    }

    enum MemoryType {
        ROM_BANK0(              0x0000, 0x3FFF),
        ROM_SWITCHABLE_BANKS(   0x4000, 0x7FFF),
        VIDEO_RAM(              0x8000, 0x9FFF),
        RAM_BANKS(              0xA000, 0xBFFF),
        RAM(                    0xC000, 0xDFFF),
        ECHO_RAM(               0xE000, 0xFDFF),
        OBJECT_ATTRIBUTE_MEMORY(0xFE00, 0xFE9F),
        UNUSABLE_MEMORY(        0xFEA0, 0xFEFF),
        IO_REGISTERS(           0xFF00, 0xFF7F),
        ZERO_PAGE(              0xFF80, 0xFFFE),
        INTERRUPT_ENABLE(       0xFFFF, 0xFFFF);

        private final static MemoryType[] valuesCache = MemoryType.values(); //TODO: do this more generic and maybe not loop through all of the always

        public final int from;
        public final int to;

        MemoryType(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public static MemoryType fromAddress(int address) {
            for (MemoryType type : valuesCache) {
                if (address >= type.from && address <= type.to) {
                    return type;
                }
            }
            throw new IllegalArgumentException("No such memory mapped " + DebugPrinter.hex(address, 4));
        }

        @Override
        public String toString() {
            return super.toString() + " (" + DebugPrinter.hex(from, 4) + "-" + DebugPrinter.hex(to, 4) + ")";
        }

        public byte[] allocate() {
            return new byte[size()];
        }

        public int size() {
            return this.to - this.from + 1;
        }
    }
}
