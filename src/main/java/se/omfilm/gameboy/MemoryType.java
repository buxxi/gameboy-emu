package se.omfilm.gameboy;

public enum MemoryType {
    ROM(0x0000, 0x3FFF),
    VIDEO_RAM(0x8000, 0x9FFF),
    IO_REGISTERS(0xFF00, 0xFF7F),
    ZERO_PAGE(0xFF80, 0xFFFE);

    public final int from;
    public final int to;

    MemoryType(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public static MemoryType fromAddress(int address) {
        for (MemoryType type : values()) {
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
