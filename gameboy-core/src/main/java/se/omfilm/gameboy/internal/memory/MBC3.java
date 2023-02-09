package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.CPU;
import se.omfilm.gameboy.internal.MMU;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.EnumByValue;
import se.omfilm.gameboy.util.Runner;

import java.time.Duration;
import java.time.Instant;

import static java.lang.Integer.compare;

public class MBC3 implements Cartridge {
    private static final Logger log = LoggerFactory.getLogger(MBC3.class);
    private static final int CLOCK_DATA_SIZE = 48;

    private final Memory rom;
    private final BankableRAM ramBanks;
    private final RealTimeClock clock;

    private int currentROMBank = 1;
    private Mode mode = Mode.RAM_BANKS;

    public MBC3(Memory rom, BankableRAM ramBanks) {
        this.rom = rom;
        this.ramBanks = ramBanks;
        this.clock = new RealTimeClock(ramBanks.clockData(CLOCK_DATA_SIZE));
    }

    public void step(int cycles) {
        for (int i = 0; i < cycles; i++) {
            clock.step();
        }
    }

    public int readByte(int address) {
        if (MMU.MemoryType.ROM_SWITCHABLE_BANKS.compareTo(address) == 0) {
            return rom.readByte(address + ((currentROMBank - 1) * MMU.MemoryType.ROM_SWITCHABLE_BANKS.size()));
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            if (mode == Mode.RAM_BANKS) {
                return ramBanks.readByte(address - MMU.MemoryType.RAM_BANKS.from);
            } else {
                return clock.read();
            }
        }
        return rom.readByte(address);
    }

    public void writeByte(int address, int data) {
        if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            if (mode == Mode.RAM_BANKS) {
                ramBanks.writeByte(address - MMU.MemoryType.RAM_BANKS.from, data);
            } else {
                clock.write(data);
            }
        } else if (address < 0x2000) {
            boolean enabled = (data & 0b0000_1010) != 0;
            ramBanks.enable(enabled);
            clock.enable(enabled);
        } else if (address < 0x4000) {
            currentROMBank = (data & 0b0111_1111);
            if (currentROMBank == 0) {
                currentROMBank += 1;
            }
        } else if (address < 0x6000) {
            if (data <= 0b0000_0011) {
                ramBanks.selectBank(data & 0b0000_0011);
                mode = Mode.RAM_BANKS;
            } else {
                clock.select(RTCRegister.fromValue(data));
                mode = Mode.REAL_TIME_CLOCK;
            }
        } else if (address < 0x8000) {
            clock.latch(data);
        } else {
            log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
        }
    }

    private static class RealTimeClock {
        private final Memory clockMemory;
        private final Runner.Counter counter = Runner.counter(this::addSecond, CPU.FREQUENCY);
        private Duration duration;
        private Duration latchedDuration;

        private RTCRegister selectedRegister = RTCRegister.SECONDS;
        private boolean enabled = false;
        private boolean latched = false;
        private boolean halted = false;

        public RealTimeClock(Memory clockMemory) {
            this.clockMemory = clockMemory;
            loadClockRAM();
        }

        public void step() {
            counter.step();
        }

        public void enable(boolean enabled) {
            this.enabled = enabled;
        }

        public void latch(int data) {
            boolean shouldLatch = data == 0x0000_0001;
            if (!latched && shouldLatch) {
                latchedDuration = duration;
            } else if (latched && !shouldLatch) {
                log.debug("Unlatching");
            }
            latched = shouldLatch;
        }

        public void select(RTCRegister register) {
            this.selectedRegister = register;
        }

        public int read() {
            if (!enabled) {
                return 0xFF;
            }
            return switch (selectedRegister) {
                case HALT_AND_DAYS_UPPER -> readHalt();
                case DAYS_LOWER -> readDays();
                case HOURS -> readHours();
                case MINUTES -> readMinutes();
                case SECONDS -> readSeconds();
            };
        }

        public void write(int data) {
            if (!enabled) {
                return;
            }
            switch (selectedRegister) {
                case HALT_AND_DAYS_UPPER -> writeHalt(data);
                case DAYS_LOWER -> writeDays(data);
                case HOURS -> writeHours(data);
                case MINUTES -> writeMinutes(data);
                case SECONDS -> writeSeconds(data);
                default -> throw new UnsupportedOperationException("Not implemented for " + selectedRegister + ": " + DebugPrinter.hex(data, 2));
            }
            persistClockRAM();
        }

        private void addSecond() {
            duration = duration.plusSeconds(1);
            persistClockRAM();
        }

        private int readHalt() {
            int days = (int) (latched ? latchedDuration : duration).toDaysPart();
            return  (days & 0b0001_0000_0000) >> 8 |
                    (halted ? 0b0100_0000 : 0) |
                    (days > 0x1FF ? 0b1000_0000 : 0);
        }

        private void writeHalt(int data) {
            halted = (data & 0b0100_0000) != 0;
            if (halted) {
                int days = ((data & 0b0000_0001) << 8) | readDays();
                duration = duration.minusDays(duration.toDaysPart()).plusDays(days);
            }
            //TODO: if first bit is set, remove the overflowing part
        }

        private int readDays() {
            int days = (int) (latched ? latchedDuration : duration).toDaysPart();
            return days & 0b1111_1111;
        }

        private void writeDays(int data) {
            if (!halted) {
                return;
            }

            int days = ((int) duration.toDaysPart() & 0b0001_0000_0000) | data;
            duration = duration.minusDays(duration.toDaysPart()).plusDays(days);
        }

        private int readHours() {
            return (latched ? latchedDuration : duration).toHoursPart();
        }

        private void writeHours(int data) {
            if (!halted) {
                return;
            }
            duration = duration.minusHours(duration.toHoursPart()).plusHours(data);
        }

        private int readMinutes() {
            return (latched ? latchedDuration : duration).toMinutesPart();
        }

        private void writeMinutes(int data) {
            if (!halted) {
                return;
            }
            duration = duration.minusMinutes(duration.toMinutesPart()).plusMinutes(data);
        }

        private int readSeconds() {
            return (latched ? latchedDuration : duration).toSecondsPart();
        }

        private void writeSeconds(int data) {
            if (!halted) {
                return;
            }
            duration = duration.minusSeconds(duration.toSecondsPart()).plusSeconds(data);
        }

        private void persistClockRAM() {
            writeClockMemoryDuration(0, duration);
            writeClockMemoryDuration(20, latchedDuration);

            long now = Instant.now().getEpochSecond();
            for (int i = 0; i < 8; i++) {
                clockMemory.writeByte(40 + i, (int) ((now >> (i * 8)) & 0xFF));
            }
        }

        private void writeClockMemoryDuration(int offsetAddress, Duration duration) {
            writeClockMemoryInt(offsetAddress, duration.toSecondsPart());
            writeClockMemoryInt(offsetAddress + 4, duration.toMinutesPart());
            writeClockMemoryInt(offsetAddress + 8, duration.toHoursPart());
            writeClockMemoryInt(offsetAddress + 12, (int) duration.toDaysPart() & 0b1111_1111);
            writeClockMemoryInt(offsetAddress + 16, (int) (duration.toDaysPart() >> 8) & 0b0000_0001);
        }

        private void writeClockMemoryInt(int offsetAddress, int data) {
            clockMemory.writeByte(offsetAddress, data);
            clockMemory.writeByte(offsetAddress + 1, 0);
            clockMemory.writeByte(offsetAddress + 2, 0);
            clockMemory.writeByte(offsetAddress + 3, 0);
        }

        private void loadClockRAM() {
            long lastTime = 0;
            for (int i = 0; i < 8; i++) {
                lastTime += clockMemory.readByte(40 + i) << (i * 8);
            }
            duration = readClockMemoryDuration(0, Duration.between(Instant.ofEpochSecond(lastTime), Instant.now()));
            latchedDuration = readClockMemoryDuration(20, Duration.ZERO);
        }

        private Duration readClockMemoryDuration(int offsetAddress, Duration duration) {
            duration = duration.plusSeconds(clockMemory.readByte(offsetAddress));
            duration = duration.plusMinutes(clockMemory.readByte(offsetAddress + 4));
            duration = duration.plusHours(clockMemory.readByte(offsetAddress + 8));
            duration = duration.plusDays(clockMemory.readByte(offsetAddress + 12));
            duration = duration.plusDays((clockMemory.readByte(offsetAddress + 16) & 0b0000_0001) << 8);
            return duration;
        }
    }

    private enum Mode {
        REAL_TIME_CLOCK,
        RAM_BANKS
    }

    private enum RTCRegister implements EnumByValue.ComparableByInt {
        SECONDS(0x08),
        MINUTES(0x09),
        HOURS(0x0A),
        DAYS_LOWER(0x0B),
        HALT_AND_DAYS_UPPER(0x0C);

        private final static EnumByValue<RTCRegister> valuesCache = EnumByValue.create(RTCRegister.values(), RTCRegister.class, RTCRegister::missing);
        private final int code;

        RTCRegister(int code) {
            this.code = code;
        }

        public static RTCRegister fromValue(int value) {
            return valuesCache.fromValue(value);
        }

        public static void missing(int value) {
            throw new IllegalArgumentException("No such " + RTCRegister.class.getName() + " with value: " + DebugPrinter.hex(value, 2));
        }

        public int compareTo(int value) {
            return compare(value, code);
        }
    }
}
