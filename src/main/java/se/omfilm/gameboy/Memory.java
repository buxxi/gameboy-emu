package se.omfilm.gameboy;

public class Memory {
    private final byte[] boot;
    private final byte[] videoRam;
    private final byte[] io;
    private final byte[] rom;
    private final byte[] zeroPage;

    private boolean isBooting = true;

    public Memory(byte[] boot, byte[] rom) {
        this.boot = boot;
        this.rom = rom;
        this.videoRam = MemoryType.VIDEO_RAM.allocate();
        this.io = MemoryType.IO.allocate();
        this.zeroPage = MemoryType.IO.allocate();
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
            case IO:
                if (address == 0xFF65) { //TODO: hardcoded value since it got stuck in an infinite loop, waiting for screen
                    return 0x90;
                }
                return unsigned(io[virtualAddress]);
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
            case IO:
                io[virtualAddress] = (byte) data;
                return;
            case ZERO_PAGE:
                zeroPage[virtualAddress] = (byte) data;
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
