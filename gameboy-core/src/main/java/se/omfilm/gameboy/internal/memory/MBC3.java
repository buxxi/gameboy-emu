package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.MMU;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.EnumByValue;

import java.time.*;

import static java.lang.Integer.compare;

public class MBC3 implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MBC3.class);

    private final Memory rom;
    private final BankableRAM ramBanks;
    private final RealTimeClock clock;

    private int currentROMBank = 1;
    private Mode mode = Mode.RAM_BANKS;

    public MBC3(Memory rom, BankableRAM ramBanks) {
        this.rom = rom;
        this.ramBanks = ramBanks;
        this.clock = new RealTimeClock(ramBanks.lastPowerOff());
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
        private boolean enabled = false;
        private RTCRegister register = RTCRegister.SECONDS;
        private int latchData = 0xFF;

        private Instant offsetTime;
        private Instant currentTime;

        public RealTimeClock(Instant initialTime) {
            offsetTime = initialTime;
            offsetTime = LocalDateTime.of(2015, 2, 1, 8, 32, 15).atZone(ZoneId.systemDefault()).toInstant();
        }

        public void enable(boolean enabled) {
            this.enabled = enabled;
        }

        public void latch(int data) {
            if (latchData == 0 && data == 1) {
                currentTime = Instant.now();
            }
            latchData = data;
        }

        public void select(RTCRegister register) {
            this.register = register;
        }

        public int read() {
            if (!enabled) {
                return 0xFF;
            }
            Duration between = Duration.between(offsetTime, currentTime);
            switch (register) {
                case SECONDS:
                    return (int) (between.getSeconds() % 60);
                case MINUTES:
                    return (int) ((between.getSeconds() / 60) % 60);
                case HOURS:
                    return (int) ((between.getSeconds() / (60 * 60)) % 24);
                case DAYS_LOWER:
                    return (int) (between.getSeconds() / (60 * 60 * 24)) & 0b1111_1111;
                case DAYS_UPPER:
                    int days = (int) (between.getSeconds() / (60 * 60 * 24));
                    log.warn("Reading " + register);
                default:
                    return 0xFF;
            }
        }

        public void write(int data) {
            if (!enabled) {
                return;
            }
            //TODO
            log.warn("Writing " + register + ": " + DebugPrinter.hex(data, 2));
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
        DAYS_UPPER(0x0C);

        private final static EnumByValue<RTCRegister> valuesCache = new EnumByValue<>(RTCRegister.values(), RTCRegister.class);
        private final int code;

        RTCRegister(int code) {
            this.code = code;
        }

        public static RTCRegister fromValue(int value) {
            RTCRegister type = valuesCache.fromValue(value);
            if (type != null) {
                return type;
            }
            throw new IllegalArgumentException("No such " + RTCRegister.class.getName() + " with value: " + DebugPrinter.hex(value, 2));
        }

        public int compareTo(int value) {
            return compare(value, code);
        }
    }
}
