package se.omfilm.gameboy;

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
        ROM(0x0000, 0x3FFF),
        VIDEO_RAM(0x8000, 0x9FFF),
        IO_REGISTERS(0xFF00, 0xFF7F),
        ZERO_PAGE(0xFF80, 0xFFFE),
        RAM(0xC000, 0xDFFF),
        INTERRUPT_ENABLE(0xFFFF, 0xFFFF);

        public final int from;
        public final int to;

        MemoryType(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public static MemoryType fromAddress(int address) {
            for (MemoryType type : MemoryType.values()) {
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
            return new byte[this.to - this.from + 1];
        }
    }
}
