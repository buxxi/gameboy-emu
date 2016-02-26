package se.omfilm.gameboy;

public class Memory {
    private final byte[] boot;
    private final byte[] rom;

    private final byte[] videoRam;
    private final byte[] zeroPage;

    private final IORegisters ioRegisters;

    private boolean isBooting = true;

    public Memory(byte[] boot, byte[] rom, IORegisters ioRegisters) {
        this.boot = boot;
        this.rom = rom;
        this.ioRegisters = ioRegisters;
        this.videoRam = MemoryType.VIDEO_RAM.allocate();
        this.zeroPage = MemoryType.ZERO_PAGE.allocate();
    }

    public int readByte(int address) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case ROM:
                if (isBooting && virtualAddress < boot.length) {
                    return unsigned(boot[virtualAddress]);
                }
                return unsigned(rom[virtualAddress]);
            case ZERO_PAGE:
                return unsigned(zeroPage[virtualAddress]);
            case VIDEO_RAM:
                return unsigned(videoRam[virtualAddress]);
            case IO_REGISTERS:
                return ioRegisters.readByte(address);
            default:
                throw new UnsupportedOperationException("Can't read from " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public int readWord(int address) {
        return readByte(address) + ((readByte(address + 1) << 8));
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case VIDEO_RAM:
                videoRam[virtualAddress] = (byte) data;
                return;
            case ZERO_PAGE:
                zeroPage[virtualAddress] = (byte) data;
                return;
            case IO_REGISTERS:
                ioRegisters.writeByte(address, data);
                return;
            default:
                throw new UnsupportedOperationException("Can't write to " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public void writeWord(int address, int data) {
        writeByte(address, data & 255);
        writeByte(address + 1, data >> 8);
    }

    private int unsigned(byte input) {
        return input & 0xFF;
    }

    public void bootSuccess() {
        isBooting = false;
    }
}
