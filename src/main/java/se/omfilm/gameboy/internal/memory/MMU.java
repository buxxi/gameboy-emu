package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.PPU;
import se.omfilm.gameboy.internal.Interrupts;
import se.omfilm.gameboy.internal.Timer;
import se.omfilm.gameboy.io.controller.Controller;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.util.DebugPrinter;

public class MMU implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MMU.class);

    private Memory rom;
    private final Memory zeroPage;
    private final ControllerMapping controllerMapping;
    private final IOMapping ioMapping;
    private final PPU ppu;
    private final SerialConnection serial;
    private final Memory ram;
    private final BankableRAM switchableRam;

    public MMU(ROM rom, PPU ppu, Interrupts interrupts, Timer timer, SerialConnection serial, Controller controller) {
        this.switchableRam = rom.createRAMBanks();
        this.rom = rom.createROMBanks(switchableRam);
        this.serial = serial;
        this.controllerMapping = new ControllerMapping(interrupts, controller);
        this.ioMapping = new IOMapping(interrupts, timer, controllerMapping);
        this.ppu = ppu;
        this.zeroPage = new ByteArrayMemory(MemoryType.ZERO_PAGE.allocate());
        this.ram = new ByteArrayMemory(MemoryType.RAM.allocate());
    }

    public int readByte(int address) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case ROM_BANK0:
            case ROM_SWITCHABLE_BANKS:
                return rom.readByte(address);
            case RAM:
            case ECHO_RAM:
                return ram.readByte(virtualAddress);
            case RAM_BANKS:
                return switchableRam.readByte(virtualAddress);
            case ZERO_PAGE:
                return zeroPage.readByte(virtualAddress);
            case VIDEO_RAM:
                return ppu.videoRAM().readByte(address);
            case OBJECT_ATTRIBUTE_MEMORY:
                return ppu.objectAttributeMemory().readByte(address);
            case INTERRUPT_ENABLE:
            case IO_REGISTERS:
                return ioMapping.readByte(address);
            case UNUSABLE_MEMORY:
                log.warn("Reading from " + MemoryType.UNUSABLE_MEMORY + " at address " + DebugPrinter.hex(address, 4));
                return 0;
            default:
                throw new UnsupportedOperationException("Can't read from " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4));
        }
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case ROM_BANK0:
            case ROM_SWITCHABLE_BANKS:
                rom.writeByte(address, data);
                return;
            case VIDEO_RAM:
                ppu.videoRAM().writeByte(address, data);
                return;
            case OBJECT_ATTRIBUTE_MEMORY:
                ppu.objectAttributeMemory().writeByte(address, data);
                return;
            case ZERO_PAGE:
                zeroPage.writeByte(virtualAddress, data);
                return;
            case IO_REGISTERS:
            case INTERRUPT_ENABLE:
                ioMapping.writeByte(address, data);
                return;
            case RAM_BANKS:
                switchableRam.writeByte(virtualAddress, data);
                return;
            case RAM:
            case ECHO_RAM:
                ram.writeByte(virtualAddress, data);
                return;
            case UNUSABLE_MEMORY:
                log.warn("Writing " + DebugPrinter.hex(data, 2) + " to " + MemoryType.UNUSABLE_MEMORY + " at " + DebugPrinter.hex(address, 4));
                return;
            default:
                throw new UnsupportedOperationException("Can't write to " + type + " for virtual address " + DebugPrinter.hex(virtualAddress, 4) + " with value " + DebugPrinter.hex(data, 4));
        }
    }

    public void step(int cycles) {
        controllerMapping.step(cycles);
    }

    public void withBootData(byte[] boot) {
        rom = new BootMemory(boot, rom);
    }

    private class ControllerMapping {
        private final Interrupts interrupts;
        private final Controller controller;

        private boolean checkDirections = false;
        private boolean checkButtons = false;
        private int controllerState = 0b0000_1111;

        public ControllerMapping(Interrupts interrupts, Controller controller) {
            this.interrupts = interrupts;
            this.controller = controller;
        }

        public void step(int cycles) {
            int controllerState = this.controllerState;
            if (checkButtons) {
                controllerState = buttonsState();
            } else if (checkDirections) {
                controllerState = directionsState();
            }

            if (this.controllerState != controllerState) {
                this.controllerState = controllerState;
                interrupts.request(Interrupts.Interrupt.JOYPAD);
            }
        }

        public int readState() {
            return controllerState;
        }

        public void writeState(int data) {
            checkButtons =      (data & 0b0010_0000) == 0;
            checkDirections =   (data & 0b0001_0000) == 0;
            if (checkButtons && checkDirections) {
                log.warn("Both buttons and direction is being checked, should not happen");
            }
        }

        private int directionsState() {
            return 0b0000_1111
                    & (controller.isPressed(Controller.Button.DOWN) ? 0b0000_0111 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.UP) ? 0b0000_1011 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.LEFT) ? 0b0000_1101 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.RIGHT) ? 0b0000_1110 : 0b0000_1111);
        }

        private int buttonsState() {
            return 0b0000_1111
                    & (controller.isPressed(Controller.Button.START) ? 0b0000_0111 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.SELECT) ? 0b0000_1011 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.B) ? 0b0000_1101 : 0b0000_1111)
                    & (controller.isPressed(Controller.Button.A) ? 0b0000_1110 : 0b0000_1111);
        }
    }

    private class IOMapping implements Memory {
        private final Interrupts interrupts;
        private final Timer timer;
        private final ControllerMapping controllerMapping;

        private IOMapping(Interrupts interrupts, Timer timer, ControllerMapping controllerMapping) {
            this.interrupts = interrupts;
            this.timer = timer;
            this.controllerMapping = controllerMapping;
        }

        public int readByte(int address) {
            IORegister register = IORegister.fromAddress(address);
            switch (register) {
                case SCROLL_Y:
                    return ppu.scrollY();
                case LCD_SCANLINE:
                    return ppu.scanline();
                case LCD_SCANLINE_COMPARE:
                    return ppu.scanlineCompare();
                case JOYPAD:
                    return controllerMapping.readState();
                case INTERRUPT_ENABLE:
                    return interrupts.enabledAsByte();
                case INTERRUPT_REQUEST:
                    return interrupts.requestedAsByte();
                case LCD_CONTROL:
                    return ppu.getLCDControl();
                case LCD_STATUS:
                    return ppu.getLCDStatus();
                case SERIAL_TRANSFER_CONTROL:
                    return serial.getControl();
                case SERIAL_TRANSFER_DATA:
                    return serial.getData();
                case PREPARE_SPEED_SWITCH:
                    return 0;
                case TIMER_COUNTER:
                    return timer.counter();
                case TIMER_DIVIDER:
                    return timer.divider();
                case TIMER_MODULO:
                    return timer.modulo();
                case SOUND_ON_OFF:
                case SOUND_CHANNEL_CONTROL:
                case SOUND_1_FREQUENCY_HIGH:
                case SOUND_2_FREQUENCY_HIGH:
                case SOUND_3_FREQUENCY_HIGH:
                case SOUND_4_COUNTER_CONSECUTIVE:
                    return 0;
                default:
                    throw new UnsupportedOperationException(unhandledReadMessage(register));
            }
        }

        public void writeByte(int address, int data) {
            IORegister register = IORegister.fromAddress(address);
            switch (register) {
                case BACKGROUND_PALETTE_DATA:
                    ppu.setBackgroundPaletteData(data);
                    return;
                case OBJECT_PALETTE_0_DATA:
                    ppu.setObjectPalette0Data(data);
                    return;
                case OBJECT_PALETTE_1_DATA:
                    ppu.setObjectPalette1Data(data);
                    return;
                case SCROLL_Y:
                    ppu.scrollY(data);
                    return;
                case SCROLL_X:
                    ppu.scrollX(data);
                    return;
                case WINDOW_Y:
                    ppu.windowY(data);
                    return;
                case WINDOW_X:
                    ppu.windowX(data);
                    return;
                case LCD_CONTROL:
                    ppu.setLCDControl(data);
                    return;
                case INTERRUPT_REQUEST:
                    interrupts.request(Interrupts.Interrupt.fromValue(data));
                    return;
                case INTERRUPT_ENABLE:
                    interrupts.enable(Interrupts.Interrupt.fromValue(data));
                    return;
                case JOYPAD:
                    controllerMapping.writeState(data);
                    return;
                case TIMER_MODULO:
                    timer.modulo(data);
                    return;
                case TIMER_CONTROL:
                    timer.control(data);
                    return;
                case TIMER_COUNTER:
                    timer.counter(data);
                    return;
                case TIMER_DIVIDER:
                    timer.resetDivider();
                    return;
                case LCD_STATUS:
                    ppu.setInterruptEnables(data);
                    return;
                case LCD_SCANLINE_COMPARE:
                    ppu.scanlineCompare(data);
                    return;
                case DMA_TRANSFER:
                    ppu.transferDMA((data * 0x100) - MemoryType.RAM.from, ram);
                    return;
                case SERIAL_TRANSFER_DATA:
                    serial.setData(data);
                    return;
                case SERIAL_TRANSFER_CONTROL:
                    serial.setControl(data);
                    return;

                case PREPARE_SPEED_SWITCH:
                case COLOR_BACKGROUND_PALETTE_INDEX:
                case COLOR_BACKGROUND_PALETTE_DATA:
                case VRAM_BANK:
                case UNKNOWN_CALLED_BY_TETRIS:
                    log.debug(unhandledWriteMessage(data, register)); //Only GameBoy Color
                    return;

                case SOUND_1_SWEEP:
                case SOUND_1_LENGTH_PATTERN_DUTY:
                case SOUND_1_ENVELOPE:
                case SOUND_1_FREQUENCY_HIGH:
                case SOUND_1_FREQUENCY_LOW:
                case SOUND_2_LENGTH_PATTERN_DUTY:
                case SOUND_2_ENVELOPE:
                case SOUND_2_FREQUENCY_LOW:
                case SOUND_2_FREQUENCY_HIGH:
                case SOUND_3_ON_OFF:
                case SOUND_3_LENGTH:
                case SOUND_3_SELECT_OUTPUT:
                case SOUND_3_FREQUENCY_LOW:
                case SOUND_3_FREQUENCY_HIGH:
                case SOUND_4_COUNTER_CONSECUTIVE:
                case SOUND_4_ENVELOPE:
                case SOUND_4_LENGTH:
                case SOUND_4_POLYNOMIAL_COUNTER:
                case SOUND_CHANNEL_CONTROL:
                case SOUND_ON_OFF:
                case SOUND_OUTPUT_TERMINAL:
                case SOUND_SWEEP:
                case SOUND_WAVE_PATTERN_RAM0:
                case SOUND_WAVE_PATTERN_RAM1:
                case SOUND_WAVE_PATTERN_RAM2:
                case SOUND_WAVE_PATTERN_RAM3:
                case SOUND_WAVE_PATTERN_RAM4:
                case SOUND_WAVE_PATTERN_RAM5:
                case SOUND_WAVE_PATTERN_RAM6:
                case SOUND_WAVE_PATTERN_RAM7:
                case SOUND_WAVE_PATTERN_RAM8:
                case SOUND_WAVE_PATTERN_RAM9:
                case SOUND_WAVE_PATTERN_RAMA:
                case SOUND_WAVE_PATTERN_RAMB:
                case SOUND_WAVE_PATTERN_RAMC:
                case SOUND_WAVE_PATTERN_RAMD:
                case SOUND_WAVE_PATTERN_RAME:
                case SOUND_WAVE_PATTERN_RAMF:
                    return; //TODO: this is only here to get somewhere without having to implement sound
                default:
                    throw new UnsupportedOperationException(unhandledWriteMessage(data, register));
            }
        }

        private String unhandledWriteMessage(int data, IORegister register) {
            return "Unhandled write for " + IOMapping.class.getSimpleName() + " of type " + register + " with value " + DebugPrinter.hex(data, 4);
        }

        private String unhandledReadMessage(IORegister register) {
            return "Unhandled read for " + IOMapping.class.getSimpleName() + " of type " + register;
        }
    }

    private enum IORegister {
        JOYPAD(0xFF00),
        SERIAL_TRANSFER_DATA(0xFF01),
        SERIAL_TRANSFER_CONTROL(0xFF02),
        TIMER_DIVIDER(0xFF04),
        TIMER_COUNTER(0xFF05),
        TIMER_MODULO(0xFF06),
        TIMER_CONTROL(0xFF07),
        INTERRUPT_REQUEST(0xFF0F),
        SOUND_1_SWEEP(0xFF10),
        SOUND_1_LENGTH_PATTERN_DUTY(0xFF11),
        SOUND_1_ENVELOPE(0xFF12),
        SOUND_1_FREQUENCY_LOW(0xFF13),
        SOUND_1_FREQUENCY_HIGH(0xFF14),
        SOUND_2_LENGTH_PATTERN_DUTY(0xFF16),
        SOUND_2_ENVELOPE(0xFF17),
        SOUND_2_FREQUENCY_LOW(0xFF18),
        SOUND_2_FREQUENCY_HIGH(0xFF19),
        SOUND_3_ON_OFF(0xFF1A),
        SOUND_3_LENGTH(0xFF1B),
        SOUND_3_SELECT_OUTPUT(0xFF1C),
        SOUND_3_FREQUENCY_LOW(0xFF1D),
        SOUND_3_FREQUENCY_HIGH(0xFF1E),
        SOUND_4_LENGTH(0xFF20),
        SOUND_4_ENVELOPE(0xFF21),
        SOUND_4_POLYNOMIAL_COUNTER(0xFF22),
        SOUND_4_COUNTER_CONSECUTIVE(0xFF23),
        SOUND_CHANNEL_CONTROL(0xFF24),
        SOUND_OUTPUT_TERMINAL(0xFF25),
        SOUND_ON_OFF(0xFF26),
        SOUND_WAVE_PATTERN_RAM0(0xFF30),
        SOUND_WAVE_PATTERN_RAM1(0xFF31),
        SOUND_WAVE_PATTERN_RAM2(0xFF32),
        SOUND_WAVE_PATTERN_RAM3(0xFF33),
        SOUND_WAVE_PATTERN_RAM4(0xFF34),
        SOUND_WAVE_PATTERN_RAM5(0xFF35),
        SOUND_WAVE_PATTERN_RAM6(0xFF36),
        SOUND_WAVE_PATTERN_RAM7(0xFF37),
        SOUND_WAVE_PATTERN_RAM8(0xFF38),
        SOUND_WAVE_PATTERN_RAM9(0xFF39),
        SOUND_WAVE_PATTERN_RAMA(0xFF3A),
        SOUND_WAVE_PATTERN_RAMB(0xFF3B),
        SOUND_WAVE_PATTERN_RAMC(0xFF3C),
        SOUND_WAVE_PATTERN_RAMD(0xFF3D),
        SOUND_WAVE_PATTERN_RAME(0xFF3E),
        SOUND_WAVE_PATTERN_RAMF(0xFF3F),
        LCD_CONTROL(0xFF40),
        LCD_STATUS(0xFF41),
        SCROLL_Y(0xFF42),
        SCROLL_X(0xFF43),
        LCD_SCANLINE(0xFF44),
        LCD_SCANLINE_COMPARE(0xFF45),
        DMA_TRANSFER(0xFF46),
        BACKGROUND_PALETTE_DATA(0xFF47),
        OBJECT_PALETTE_0_DATA(0xFF48),
        OBJECT_PALETTE_1_DATA(0xFF49),
        WINDOW_Y(0xFF4A),
        WINDOW_X(0xFF4B),
        PREPARE_SPEED_SWITCH(0xFF4D), //Only GBC
        VRAM_BANK(0xFF4F), //Only GBC
        SOUND_SWEEP(0xFF50),
        COLOR_BACKGROUND_PALETTE_INDEX(0xFF68), //Only GBC
        COLOR_BACKGROUND_PALETTE_DATA(0xFF69), //Only GBC
        UNKNOWN_CALLED_BY_TETRIS(0xFF7F),

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

    private class BootMemory implements Memory {
        private final ByteArrayMemory boot;
        private final Memory delegate;

        public BootMemory(byte[] boot, Memory delegate) {
            this.boot = new ByteArrayMemory(boot);
            this.delegate = delegate;
        }

        public int readByte(int address) {
            if (address <= 0xFF) {
                return boot.readByte(address);
            } else if (address == 0x100) {
                rom = delegate;
            }
            return delegate.readByte(address);

        }

        public void writeByte(int address, int data) {
            delegate.writeByte(address, data);
        }
    }
}
