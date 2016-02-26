package se.omfilm.gameboy;

public class IORegisters {
    private int soundOnOff = 0;
    private int soundFrequencyLow = 0;
    private int soundFrequencyHigh = 0;
    private int soundLengthPatternDuty = 0;
    private int soundEnvelope = 0;
    private int soundOutputTerminal = 0;
    private int soundChannelControl = 0;
    private int paletteData = 0;
    private int soundSweep = 0;
    private int scrollY = 0;
    private int lcdControl = 0;

    public int readByte(int address) {
        IORegister register = IORegister.fromAddress(address);
        switch (register) {
            case SCROLL_Y:
                return scrollY;
            case LCD_Y:
                return 144; //TODO: fix hardcoded value
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
            case PALETTE_DATA:
                paletteData = data;
                return;
            case SCROLL_Y:
                scrollY = data;
                return;
            case LCD_CONTROL:
                lcdControl = data;
                return;
            default:
                throw new UnsupportedOperationException("Unhandled write of value " + DebugPrinter.hex(data, 2) + " for " + IORegister.class.getSimpleName() + " of type " + register);
        }
    }

    private enum IORegister {
        SOUND_LENGTH_PATTERN_DUTY(0xFF11),
        SOUND_ENVELOPE(0xFF12),
        SOUND_FREQUENCY_LOW(0xFF13),
        SOUND_FREQUENCY_HIGH(0xFF14),
        SOUND_CHANNEL_CONTROL(0xFF24),
        SOUND_OUTPUT_TERMINAL(0xFF25),
        SOUND_ON_OFF(0xFF26),
        LCD_CONTROL(0xFF40),
        SCROLL_Y(0xFF42),
        LCD_Y(0xFF44),
        PALETTE_DATA(0xFF47),
        SOUND_SWEEP(0xFF50);

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
