package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.io.IOController;
import se.omfilm.gameboy.util.DebugPrinter;

public class MMU implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MMU.class);

    private final Memory boot;
    private final Memory rom;
    private final Memory zeroPage;
    private final Memory ioController;
    private final Memory gpu;
    private final Memory ram;

    private boolean isBooting = true;

    public MMU(Memory boot, Memory rom, IOController ioController, Memory gpu) {
        this.boot = boot;
        this.rom = rom;
        this.ioController = ioController;
        this.gpu = gpu;
        this.zeroPage = new ByteArrayMemory(MemoryType.ZERO_PAGE.allocate());
        this.ram = new ByteArrayMemory(MemoryType.RAM.allocate());
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
            case RAM:
                return ram.readByte(virtualAddress);
            case ZERO_PAGE:
                return zeroPage.readByte(virtualAddress);
            case VIDEO_RAM:
                return gpu.readByte(virtualAddress);
            case INTERRUPT_ENABLE:
            case IO_REGISTERS:
                return ioController.readByte(address);
            default:
                throw new UnsupportedOperationException("Can't read from " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public void writeByte(int address, int data) {
        if (address == 0x2000) {
            log.warn("ROM Banking not implemented but called with " + DebugPrinter.hex(data, 4));
            return;
        }
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case VIDEO_RAM:
            case OBJECT_ATTRIBUTE_MEMORY:
                gpu.writeByte(address, data);
                return;
            case ZERO_PAGE:
                zeroPage.writeByte(virtualAddress, data);
                return;
            case IO_REGISTERS:
            case INTERRUPT_ENABLE:
                ioController.writeByte(address, data);
                return;
            case RAM:
            case ECHO_RAM:
                ram.writeByte(virtualAddress, data);
                return;
            default:
                throw new UnsupportedOperationException("Can't write to " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4) + " with value " + DebugPrinter.hex(data, 4));
        }
    }

    public void bootSuccess() {
        isBooting = false;
    }
}
