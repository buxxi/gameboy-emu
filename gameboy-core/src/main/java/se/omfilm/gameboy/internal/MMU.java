package se.omfilm.gameboy.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.memory.ByteArrayMemory;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.internal.memory.ROM;
import se.omfilm.gameboy.io.serial.SerialConnection;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.EnumByValue;

public class MMU implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MMU.class);

    private Memory rom;
    private final Memory zeroPage;
    private final Input input;
    private final PPU ppu;
    private final APU apu;
    private final Interrupts interrupts;
    private final Timer timer;
    private final SerialConnection serial;
    private final Memory ram;

    public MMU(ROM rom, PPU ppu, APU apu, Interrupts interrupts, Timer timer, SerialConnection serial, Input input) {
        this.rom = rom.createROMBanks();
        this.apu = apu;
        this.interrupts = interrupts;
        this.timer = timer;
        this.serial = serial;
        this.input = input;
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
            case RAM_BANKS:
                return rom.readByte(address);
            case RAM:
            case ECHO_RAM:
                return ram.readByte(virtualAddress);
            case ZERO_PAGE:
                return zeroPage.readByte(virtualAddress);
            case VIDEO_RAM:
                return ppu.videoRAM().readByte(address);
            case OBJECT_ATTRIBUTE_MEMORY:
                return ppu.objectAttributeMemory().readByte(address);
            case INTERRUPT_ENABLE:
            case IO_REGISTERS:
                try {
                    return IORegister.fromAddress(address).read(MMU.this);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                    return 0xFF;
                }
            default:
                log.warn("Reading from " + type + " at address " + DebugPrinter.hex(address, 4));
                return 0xFF;
        }
    }

    public void writeByte(int address, int data) {
        MemoryType type = MemoryType.fromAddress(address);
        int virtualAddress = address - type.from;
        switch (type) {
            case ROM_BANK0:
            case ROM_SWITCHABLE_BANKS:
            case RAM_BANKS:
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
                try {
                    IORegister.fromAddress(address).write(MMU.this, data);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
                return;
            case RAM:
            case ECHO_RAM:
                ram.writeByte(virtualAddress, data);
                return;
            default:
                log.warn("Writing " + DebugPrinter.hex(data, 2) + " to " + MemoryType.UNUSABLE_MEMORY + " at " + DebugPrinter.hex(address, 4));
        }
    }

    public void withBootData(byte[] boot) {
        rom = new BootMemory(boot, rom);
    }

    private int readWaveRAM(IORegister register) {
        return apu.wavePatternRAM().readByte(register.address);
    }

    private void writeWaveRAM(IORegister register, int data) {
        apu.wavePatternRAM().writeByte(register.address, data);
    }

    private int invalidRead(IORegister reg) {
        log.warn("Reading from " + reg + " is not supported");
        return 0xFF;
    }

    private void invalidWrite(IORegister reg, int data) {
        log.warn("Writing " + DebugPrinter.hex(data, 2) + " to " + reg + " is not supported");
    }

    private int requestedInterrupts() {
        int result = 0;
        for (Interrupts.Interrupt i : Interrupts.Interrupt.cachedValues()) {
            if (interrupts.requested(i)) {
                result = result | i.mask();
            }
        }
        return result;
    }

    private void requestedInterrupts(int data) {
        for (Interrupts.Interrupt i : Interrupts.Interrupt.cachedValues()) {
            interrupts.request(i, (data & i.mask()) != 0);
        }
    }

    private int enabledInterrupts() {
        int result = 0;
        for (Interrupts.Interrupt i : Interrupts.Interrupt.cachedValues()) {
            if (interrupts.enabled(i)) {
                result = result | i.mask();
            }
        }
        return result;
    }

    private void enabledInterrupts(int data) {
        for (Interrupts.Interrupt i : Interrupts.Interrupt.cachedValues()) {
            interrupts.enable(i, (data & i.mask()) != 0);
        }
    }

    private enum IORegister implements EnumByValue.ComparableByInt {
        JOYPAD(0xFF00,
                (mmu, reg) -> mmu.input.readState(),
                (mmu, reg, data) -> mmu.input.writeState(data)
        ),
        SERIAL_TRANSFER_DATA(0xFF01,
                (mmu, reg) -> mmu.serial.data(),
                (mmu, reg, data) -> mmu.serial.data(data)
        ),
        SERIAL_TRANSFER_CONTROL(0xFF02,
                (mmu, reg) -> mmu.serial.control(),
                (mmu, reg, data) -> mmu.serial.control(data)
        ),
        TIMER_DIVIDER(0xFF04,
                (mmu, reg) -> mmu.timer.divider(),
                (mmu, reg, data) -> mmu.timer.resetDivider()
        ),
        TIMER_COUNTER(0xFF05,
                (mmu, reg) -> mmu.timer.counter(),
                (mmu, reg, data) -> mmu.timer.counter(data)
        ),
        TIMER_MODULO(0xFF06,
                (mmu, reg) -> mmu.timer.modulo(),
                (mmu, reg, data) -> mmu.timer.modulo(data)
        ),
        TIMER_CONTROL(0xFF07,
                (mmu, reg) -> mmu.timer.control(),
                (mmu, reg, data) -> mmu.timer.control(data)
        ),
        INTERRUPT_REQUEST(0xFF0F,
                (mmu, reg) -> mmu.requestedInterrupts(),
                (mmu, reg, data) -> mmu.requestedInterrupts(data)
        ),
        SOUND_1_SWEEP(0xFF10,
                (mmu, reg) -> mmu.apu.sweep(APU.SoundId.SOUND1_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.sweep(APU.SoundId.SOUND1_SQUARE_WAVE, data)
        ),
        SOUND_1_LENGTH_PATTERN_DUTY(0xFF11,
                (mmu, reg) -> mmu.apu.length(APU.SoundId.SOUND1_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.length(APU.SoundId.SOUND1_SQUARE_WAVE, data)
        ),
        SOUND_1_ENVELOPE(0xFF12,
                (mmu, reg) -> mmu.apu.envelope(APU.SoundId.SOUND1_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.envelope(APU.SoundId.SOUND1_SQUARE_WAVE, data)
        ),
        SOUND_1_FREQUENCY_LOW(0xFF13,
                MMU::invalidRead,
                (mmu, reg, data) -> mmu.apu.lowFrequency(APU.SoundId.SOUND1_SQUARE_WAVE, data)
        ),
        SOUND_1_FREQUENCY_HIGH(0xFF14,
                (mmu, reg) -> mmu.apu.highFrequency(APU.SoundId.SOUND1_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.highFrequency(APU.SoundId.SOUND1_SQUARE_WAVE, data)
        ),
        SOUND_2_LENGTH_PATTERN_DUTY(0xFF16,
                (mmu, reg) -> mmu.apu.length(APU.SoundId.SOUND2_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.length(APU.SoundId.SOUND2_SQUARE_WAVE, data)
        ),
        SOUND_2_ENVELOPE(0xFF17,
                (mmu, reg) -> mmu.apu.envelope(APU.SoundId.SOUND2_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.envelope(APU.SoundId.SOUND2_SQUARE_WAVE, data)
        ),
        SOUND_2_FREQUENCY_LOW(0xFF18,
                MMU::invalidRead,
                (mmu, reg, data) -> mmu.apu.lowFrequency(APU.SoundId.SOUND2_SQUARE_WAVE, data)
        ),
        SOUND_2_FREQUENCY_HIGH(0xFF19,
                (mmu, reg) -> mmu.apu.highFrequency(APU.SoundId.SOUND2_SQUARE_WAVE),
                (mmu, reg, data) -> mmu.apu.highFrequency(APU.SoundId.SOUND2_SQUARE_WAVE, data)
        ),
        SOUND_3_ON_OFF(0xFF1A,
                (mmu, reg) -> mmu.apu.soundControl(APU.SoundId.SOUND3_WAVE),
                (mmu, reg, data) -> mmu.apu.soundControl(APU.SoundId.SOUND3_WAVE, data)
        ),
        SOUND_3_LENGTH(0xFF1B,
                (mmu, reg) -> mmu.apu.length(APU.SoundId.SOUND3_WAVE),
                (mmu, reg, data) -> mmu.apu.length(APU.SoundId.SOUND3_WAVE, data)
        ),
        SOUND_3_SELECT_OUTPUT_LEVEL(0xFF1C,
                (mmu, reg) -> mmu.apu.outputLevel(APU.SoundId.SOUND3_WAVE),
                (mmu, reg, data) -> mmu.apu.outputLevel(APU.SoundId.SOUND3_WAVE, data)
        ),
        SOUND_3_FREQUENCY_LOW(0xFF1D,
                MMU::invalidRead,
                (mmu, reg, data) -> mmu.apu.lowFrequency(APU.SoundId.SOUND3_WAVE, data)
        ),
        SOUND_3_FREQUENCY_HIGH(0xFF1E,
                (mmu, reg) -> mmu.apu.highFrequency(APU.SoundId.SOUND3_WAVE),
                (mmu, reg, data) -> mmu.apu.highFrequency(APU.SoundId.SOUND3_WAVE, data)
        ),
        SOUND_4_LENGTH(0xFF20,
                (mmu, reg) -> mmu.apu.length(APU.SoundId.SOUND4_NOISE),
                (mmu, reg, data) -> mmu.apu.length(APU.SoundId.SOUND4_NOISE, data)
        ),
        SOUND_4_ENVELOPE(0xFF21,
                (mmu, reg) -> mmu.apu.envelope(APU.SoundId.SOUND4_NOISE),
                (mmu, reg, data) -> mmu.apu.envelope(APU.SoundId.SOUND4_NOISE, data)
        ),
        SOUND_4_POLYNOMIAL_COUNTER(0xFF22,
                (mmu, reg) -> mmu.apu.polynomialCounter(APU.SoundId.SOUND4_NOISE),
                (mmu, reg, data) -> mmu.apu.polynomialCounter(APU.SoundId.SOUND4_NOISE, data)
        ),
        SOUND_4_COUNTER_CONSECUTIVE(0xFF23,
                (mmu, reg) -> mmu.apu.soundMode(APU.SoundId.SOUND4_NOISE),
                (mmu, reg, data) -> mmu.apu.soundMode(APU.SoundId.SOUND4_NOISE, data)
        ),
        SOUND_CHANNEL_CONTROL(0xFF24,
                (mmu, reg) -> mmu.apu.channelControl(),
                (mmu, reg, data) -> mmu.apu.channelControl(data)
        ),
        SOUND_OUTPUT_TERMINAL(0xFF25,
                (mmu, reg) -> mmu.apu.outputTerminal(),
                (mmu, reg, data) -> mmu.apu.outputTerminal(data)
        ),
        SOUND_ON_OFF(0xFF26,
                (mmu, reg) -> mmu.apu.soundEnabled(),
                (mmu, reg, data) -> mmu.apu.soundEnabled(data)
        ),
        SOUND_WAVE_PATTERN_RAM_0(0xFF30, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_1(0xFF31, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_2(0xFF32, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_3(0xFF33, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_4(0xFF34, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_5(0xFF35, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_6(0xFF36, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_7(0xFF37, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_8(0xFF38, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_9(0xFF39, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_A(0xFF3A, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_B(0xFF3B, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_C(0xFF3C, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_D(0xFF3D, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_E(0xFF3E, MMU::readWaveRAM, MMU::writeWaveRAM),
        SOUND_WAVE_PATTERN_RAM_F(0xFF3F, MMU::readWaveRAM, MMU::writeWaveRAM),
        LCD_CONTROL(0xFF40,
                (mmu, reg) -> mmu.ppu.control(),
                (mmu, reg, data) -> mmu.ppu.control(data)
        ),
        LCD_STATUS(0xFF41,
                (mmu, reg) -> mmu.ppu.status(),
                (mmu, reg, data) -> mmu.ppu.interruptEnables(data)
        ),
        SCROLL_Y(0xFF42,
                (mmu, reg) -> mmu.ppu.scrollY(),
                (mmu, reg, data) -> mmu.ppu.scrollY(data)
        ),
        SCROLL_X(0xFF43,
                (mmu, reg) -> mmu.ppu.scrollX(),
                (mmu, reg, data) -> mmu.ppu.scrollX(data)
        ),
        LCD_SCANLINE(0xFF44,
                (mmu, reg) -> mmu.ppu.scanline(),
                MMU::invalidWrite
        ),
        LCD_SCANLINE_COMPARE(0xFF45,
                (mmu, reg) -> mmu.ppu.scanlineCompare(),
                (mmu, reg, data) -> mmu.ppu.scanlineCompare(data)
        ),
        DMA_TRANSFER(0xFF46,
                MMU::invalidRead,
                (mmu, reg, data) -> mmu.ppu.transferDMA((data * 0x100) - MemoryType.RAM.from, mmu.ram)
        ),
        BACKGROUND_PALETTE_DATA(0xFF47,
                (mmu, reg) -> mmu.ppu.backgroundPalette(),
                (mmu, reg, data) -> mmu.ppu.backgroundPalette(data)
        ),
        OBJECT_PALETTE_0_DATA(0xFF48,
                (mmu, reg) -> mmu.ppu.objectPalette0(),
                (mmu, reg, data) -> mmu.ppu.objectPalette0(data)
        ),
        OBJECT_PALETTE_1_DATA(0xFF49,
                (mmu, reg) -> mmu.ppu.objectPalette1(),
                (mmu, reg, data) -> mmu.ppu.objectPalette1(data)
        ),
        WINDOW_Y(0xFF4A,
                (mmu, reg) -> mmu.ppu.windowY(),
                (mmu, reg, data) -> mmu.ppu.windowY(data)
        ),
        WINDOW_X(0xFF4B,
                (mmu, reg) -> mmu.ppu.windowX(),
                (mmu, reg, data) -> mmu.ppu.windowX(data)
        ),
        BOOT_SUCCESS(0xFF50,
                MMU::invalidRead,
                (mmu, reg, data) -> mmu.rom.writeByte(0xFF50, data)
        ),
        INTERRUPT_ENABLE(0xFFFF,
                (mmu, reg) -> mmu.enabledInterrupts(),
                (mmu, reg, data) -> mmu.enabledInterrupts(data)
        );

        private final static EnumByValue<IORegister> valuesCache = new EnumByValue<>(values(), IORegister.class);
        private final int address;
        private final IOReader reader;
        private final IOWriter writer;

        IORegister(int address, IOReader reader, IOWriter writer) {
            this.address = address;
            this.reader = reader;
            this.writer = writer;
        }

        public int read(MMU mmu) {
            return reader.read(mmu, this);
        }

        public void write(MMU mmu, int data) {
            writer.write(mmu, this, data);
        }

        @Override
        public String toString() {
            return super.toString() + " (" + DebugPrinter.hex(address, 4) + ")";
        }

        public static IORegister fromAddress(int address) {
            IORegister reg = valuesCache.fromValue(address);
            if (reg != null) {
                return reg;
            }
            throw new IllegalArgumentException("No " + IORegister.class.getSimpleName() + " for address " + DebugPrinter.hex(address, 4));
        }

        public int compareTo(int value) {
            return value - address;
        }

        private interface IOReader {
            int read(MMU mmu, IORegister register);
        }

        private interface IOWriter {
            void write(MMU mmu, IORegister register, int data);
        }
    }

    public enum MemoryType implements EnumByValue.ComparableByInt {
        ROM_BANK0(              0x0000, 0x3FFF),
        ROM_SWITCHABLE_BANKS(   0x4000, 0x7FFF),
        VIDEO_RAM(              0x8000, 0x9FFF),
        RAM_BANKS(              0xA000, 0xBFFF),
        RAM(                    0xC000, 0xDFFF),
        ECHO_RAM(               0xE000, 0xFDFF),
        OBJECT_ATTRIBUTE_MEMORY(0xFE00, 0xFE9F),
        UNUSABLE_MEMORY(        0xFEA0, 0xFEFF),
        IO_REGISTERS(           0xFF00, 0xFF7F),
        ZERO_PAGE(              0xFF80, 0xFFFE),
        INTERRUPT_ENABLE(       0xFFFF, 0xFFFF);

        private final static EnumByValue<MemoryType> valuesCache = new EnumByValue<>(MemoryType.values(), MemoryType.class);

        public final int from;
        public final int to;

        MemoryType(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public static MemoryType fromAddress(int address) {
            MemoryType type = valuesCache.fromValue(address);
            if (type != null) {
                return type;
            }
            throw new IllegalArgumentException("No such memory mapped " + DebugPrinter.hex(address, 4));
        }

        @Override
        public String toString() {
            return super.toString() + " (" + DebugPrinter.hex(from, 4) + "-" + DebugPrinter.hex(to, 4) + ")";
        }

        public int compareTo(int value) {
            if (value < from) {
                return value - from;
            } else if (value > to) {
                return value - to;
            }
            return 0;
        }

        public byte[] allocate() {
            return new byte[size()];
        }

        public int size() {
            return this.to - this.from + 1;
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
            }
            return delegate.readByte(address);

        }

        public void writeByte(int address, int data) {
            if (address == IORegister.BOOT_SUCCESS.address) {
                rom = delegate;
                return;
            }
            delegate.writeByte(address, data);
        }
    }
}
