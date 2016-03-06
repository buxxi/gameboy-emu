package se.omfilm.gameboy.io;

import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.Memory;

public class IOController implements Memory {
    private final GPU gpu;
    private int soundOnOff = 0;
    private int soundFrequencyLow = 0;
    private int soundFrequencyHigh = 0;
    private int soundLengthPatternDuty = 0;
    private int soundEnvelope = 0;
    private int soundOutputTerminal = 0;
    private int soundChannelControl = 0;
    private int soundSweep = 0;

    public IOController(GPU gpu) {
        this.gpu = gpu;
    }

    public int readByte(int address) {
        IORegister register = IORegister.fromAddress(address);
        switch (register) {
            case SCROLL_Y:
                return gpu.scrollY();
            case LCD_SCANLINE:
                return gpu.scanline();
            default:
                throw new UnsupportedOperationException("Unhandled read for " + IORegister.class.getSimpleName() + " of type " + register);
        }
    }

    public void writeByte(int address, int data) {
        IORegister register = IORegister.fromAddress(address);
        switch (register) {
            case SOUND_ON_OFF:
                soundOnOff = data;
                return;
            case SOUND_FREQUENCY_LOW:
                soundFrequencyLow = data;
                return;
            case SOUND_FREQUENCY_HIGH:
                soundFrequencyHigh = data;
                return;
            case SOUND_LENGTH_PATTERN_DUTY:
                soundLengthPatternDuty = data;
                return;
            case SOUND_ENVELOPE:
                soundEnvelope = data;
                return;
            case SOUND_OUTPUT_TERMINAL:
                soundOutputTerminal = data;
                return;
            case SOUND_CHANNEL_CONTROL:
                soundChannelControl = data;
                return;
            case SOUND_SWEEP:
                soundSweep = data;
                return;
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
            case LCD_STATUS:
                System.out.println(IORegister.LCD_STATUS + " not implemented, but called with value " + DebugPrinter.hex(data, 4)); //TODO
                return;
            case LCD_CONTROL:
                gpu.setLCDControl(data);
                return;
            case INTERRUPT:
                System.out.println(IORegister.INTERRUPT + " not implemented, but called with value " + DebugPrinter.hex(data, 4)); //TODO
                return;
            case SERIAL_TRANSFER_DATA:
                System.out.println(IORegister.SERIAL_TRANSFER_DATA + " not implemented, but called with value " + DebugPrinter.hex(data, 4)); //TODO
                return;
            case SERIAL_TRANSFER_CONTROL:
                System.out.println(IORegister.SERIAL_TRANSFER_CONTROL + " not implemented, but called with value " + DebugPrinter.hex(data, 4)); //TODO
                return;
            case UNKNOWN:
                return;
            default:
                throw new UnsupportedOperationException("Unhandled write of value " + DebugPrinter.hex(data, 2) + " for " + IORegister.class.getSimpleName() + " of type " + register);
        }
    }

    private enum IORegister {
        SERIAL_TRANSFER_DATA(0xFF01),
        SERIAL_TRANSFER_CONTROL(0xFF02),
        INTERRUPT(0xFF0F),
        SOUND_LENGTH_PATTERN_DUTY(0xFF11),
        SOUND_ENVELOPE(0xFF12),
        SOUND_FREQUENCY_LOW(0xFF13),
        SOUND_FREQUENCY_HIGH(0xFF14),
        SOUND_CHANNEL_CONTROL(0xFF24),
        SOUND_OUTPUT_TERMINAL(0xFF25),
        SOUND_ON_OFF(0xFF26),
        LCD_CONTROL(0xFF40),
        LCD_STATUS(0xFF41),
        SCROLL_Y(0xFF42),
        SCROLL_X(0xFF43),
        LCD_SCANLINE(0xFF44),
        BACKGROUND_PALETTE_DATA(0xFF47),
        OBJECT_PALETTE_0_DATA(0xFF48),
        OBJECT_PALETTE_1_DATA(0xFF49),
        SOUND_SWEEP(0xFF50),
        UNKNOWN(0xFF7F);

        private final int address;

        IORegister(int address) {
            this.address = address;
        }

        private static IORegister fromAddress(int address) {
            for (IORegister register : values()) {
                if (register.address == address) {
                    return register;
                }
            }
            throw new IllegalArgumentException("No " + IORegister.class.getSimpleName() + " for address " + DebugPrinter.hex(address, 4));
        }
    }
}
