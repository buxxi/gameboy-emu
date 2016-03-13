package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class MMU implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MMU.class);

    private final Memory boot;
    private final Memory rom;
    private final Memory zeroPage;
    private final Memory ioController;
    private final GPU gpu;
    private final Memory ram;
    private final Memory[] switchableRam;

    private boolean isBooting = true;

    public MMU(Memory boot, Memory rom, GPU gpu, Interrupts interrupts, Timer timer) {
        this.boot = boot;
        this.rom = rom;
        this.ioController = new IOController(interrupts, timer);
        this.gpu = gpu;
        this.zeroPage = new ByteArrayMemory(MemoryType.ZERO_PAGE.allocate());
        this.ram = new ByteArrayMemory(MemoryType.RAM.allocate());
        this.switchableRam = new Memory[1];
        this.switchableRam[0] = new ByteArrayMemory(MemoryType.RAM_BANKS.allocate()); //TODO: handle multiple ram banks
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
            case RAM_BANKS:
                return switchableRam[0].readByte(virtualAddress);
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

    private class IOController implements Memory {
        private final Interrupts interrupts;
        private final Timer timer;

        private int joypad = 0; //TODO

        private IOController(Interrupts interrupts, Timer timer) {
            this.interrupts = interrupts;
            this.timer = timer;
        }

        public int readByte(int address) {
            IORegister register = IORegister.fromAddress(address);
            switch (register) {
                case SCROLL_Y:
                    return gpu.scrollY();
                case LCD_SCANLINE:
                    return gpu.scanline();
                case JOYPAD:
                    return joypad;
                case INTERRUPT_ENABLE:
                    return Interrupts.Interrupt.toValue(interrupts);
                default:
                    throw new UnsupportedOperationException(unhandledReadMessage(register));
            }
        }

        public void writeByte(int address, int data) {
            IORegister register = IORegister.fromAddress(address);
            switch (register) {
                case BACKGROUND_PALETTE_DATA:
                    gpu.setBackgroundPaletteData(data);
                    return;
                case OBJECT_PALETTE_0_DATA:
                    gpu.setObjectPalette0Data(data);
                    return;
                case OBJECT_PALETTE_1_DATA:
                    gpu.setObjectPalette1Data(data);
                    return;
                case SCROLL_Y:
                    gpu.scrollY(data);
                    return;
                case SCROLL_X:
                    gpu.scrollX(data);
                    return;
                case WINDOW_Y:
                    gpu.windowY(data);
                    return;
                case WINDOW_X:
                    gpu.windowX(data);
                    return;
                case LCD_CONTROL:
                    gpu.setLCDControl(data);
                    return;
                case UNKNOWN:
                    return;
                case INTERRUPT_REQUEST:
                    interrupts.request(Interrupts.Interrupt.fromValue(data));
                    return;
                case INTERRUPT_ENABLE:
                    interrupts.enable(Interrupts.Interrupt.fromValue(data));
                    return;
                case JOYPAD:
                    joypad = data;
                    return;
                case TIMER_MODULO:
                    timer.setModulo(data);
                    return;
                case LCD_STATUS:
                    gpu.setInterruptEnables(data);
                    return;
                case DMA_TRANSFER:
                    gpu.transferDMA((data * 0x100) - MemoryType.RAM.from, ram);
                    return;
                case SERIAL_TRANSFER_DATA:
                case SERIAL_TRANSFER_CONTROL:

                case SOUND_1_SWEEP:
                case SOUND_1_LENGTH_PATTERN_DUTY:
                case SOUND_1_ENVELOPE:
                case SOUND_1_FREQUENCY_HIGH:
                case SOUND_1_FREQUENCY_LOW:
                case SOUND_2_ENVELOPE:
                case SOUND_2_FREQUENCY_HIGH:
                case SOUND_3_ON_OFF:
                case SOUND_4_COUNTER_CONSECUTIVE:
                case SOUND_4_ENVELOPE:
                case SOUND_CHANNEL_CONTROL:
                case SOUND_ON_OFF:
                case SOUND_OUTPUT_TERMINAL:
                case SOUND_SWEEP:
                    log.warn(unhandledWriteMessage(data, register)); //TODO: this is only here to get somewhere without having to implement sound
                    return;
                default:
                    throw new UnsupportedOperationException(unhandledWriteMessage(data, register));
            }
        }

        private String unhandledWriteMessage(int data, IORegister register) {
            return "Unhandled write for " + IOController.class.getSimpleName() + " of type " + register + " with value " + DebugPrinter.hex(data, 4);
        }

        private String unhandledReadMessage(IORegister register) {
            return "Unhandled read for " + IOController.class.getSimpleName() + " of type " + register;
        }
    }

    private enum IORegister {
        JOYPAD(0xFF00),
        SERIAL_TRANSFER_DATA(0xFF01),
        SERIAL_TRANSFER_CONTROL(0xFF02),
        TIMER_MODULO(0xFF06),
        INTERRUPT_REQUEST(0xFF0F),
        SOUND_1_SWEEP(0xFF10),
        SOUND_1_LENGTH_PATTERN_DUTY(0xFF11),
        SOUND_1_ENVELOPE(0xFF12),
        SOUND_1_FREQUENCY_LOW(0xFF13),
        SOUND_1_FREQUENCY_HIGH(0xFF14),
        SOUND_2_ENVELOPE(0xFF17),
        SOUND_2_FREQUENCY_HIGH(0xFF19),
        SOUND_3_ON_OFF(0xFF1A),
        SOUND_4_ENVELOPE(0xFF21),
        SOUND_4_COUNTER_CONSECUTIVE(0xFF23),
        SOUND_CHANNEL_CONTROL(0xFF24),
        SOUND_OUTPUT_TERMINAL(0xFF25),
        SOUND_ON_OFF(0xFF26),
        LCD_CONTROL(0xFF40),
        LCD_STATUS(0xFF41),
        SCROLL_Y(0xFF42),
        SCROLL_X(0xFF43),
        LCD_SCANLINE(0xFF44),
        DMA_TRANSFER(0xFF46),
        BACKGROUND_PALETTE_DATA(0xFF47),
        OBJECT_PALETTE_0_DATA(0xFF48),
        OBJECT_PALETTE_1_DATA(0xFF49),
        WINDOW_Y(0xFF4A),
        WINDOW_X(0xFF4B),
        SOUND_SWEEP(0xFF50),
        UNKNOWN(0xFF7F),
        INTERRUPT_ENABLE(0xFFFF);

        private final int address;

        IORegister(int address) {
            this.address = address;
        }

        private static IORegister fromAddress(int address) {
            for (IORegister register : IORegister.values()) {
                if (register.address == address) {
                    return register;
                }
            }
            throw new IllegalArgumentException("No " + IORegister.class.getSimpleName() + " for address " + DebugPrinter.hex(address, 4));
        }
    }
}
