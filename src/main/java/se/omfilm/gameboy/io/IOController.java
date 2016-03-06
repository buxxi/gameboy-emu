package se.omfilm.gameboy.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.Memory;

public class IOController implements Memory {
    private static final Logger log = LoggerFactory.getLogger(IOController.class);
    private final GPU gpu;

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
                throw new UnsupportedOperationException("Unhandled read for " + IOController.class.getSimpleName() + " of type " + register);
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
            case LCD_CONTROL:
                gpu.setLCDControl(data);
                return;
            case UNKNOWN:
                return;
            case LCD_STATUS:
            case INTERRUPT:
            case INTERRUPT_ENABLE:
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

    private enum IORegister {
        SERIAL_TRANSFER_DATA(0xFF01),
        SERIAL_TRANSFER_CONTROL(0xFF02),
        INTERRUPT(0xFF0F),
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
        BACKGROUND_PALETTE_DATA(0xFF47),
        OBJECT_PALETTE_0_DATA(0xFF48),
        OBJECT_PALETTE_1_DATA(0xFF49),
        SOUND_SWEEP(0xFF50),
        UNKNOWN(0xFF7F),
        INTERRUPT_ENABLE(0xFFFF);

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
