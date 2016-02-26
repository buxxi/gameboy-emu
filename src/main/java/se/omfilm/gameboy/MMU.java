package se.omfilm.gameboy;

public class MMU implements Memory {
    private final Memory boot;
    private final Memory rom;
    private final Memory zeroPage;
    private final Memory ioController;
    private final Memory gpu;

    private boolean isBooting = true;

    public MMU(Memory boot, Memory rom, IOController ioController, Memory gpu) {
        this.boot = boot;
        this.rom = rom;
        this.ioController = ioController;
        this.gpu = gpu;
        this.zeroPage = new ByteArrayMemory(MemoryType.ZERO_PAGE.allocate());
    }

    public int readByte(int address) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case ROM:
                if (isBooting && virtualAddress <= 0xFF) {
                    return boot.readByte(virtualAddress);
                }
                return rom.readByte(virtualAddress);
            case ZERO_PAGE:
                return zeroPage.readByte(virtualAddress);
            case VIDEO_RAM:
                return gpu.readByte(virtualAddress);
            case IO_REGISTERS:
                return ioController.readByte(address);
            default:
                throw new UnsupportedOperationException("Can't read from " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case VIDEO_RAM:
                gpu.writeByte(virtualAddress, data);
                return;
            case ZERO_PAGE:
                zeroPage.writeByte(virtualAddress, data);
                return;
            case IO_REGISTERS:
                ioController.writeByte(address, data);
                return;
            default:
                throw new UnsupportedOperationException("Can't write to " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public void bootSuccess() {
        isBooting = false;
    }
}
