package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.MMU;
import se.omfilm.gameboy.util.DebugPrinter;

public class MBC1 implements Cartridge {
    private static final Logger log = LoggerFactory.getLogger(MBC1.class);
    private final Memory rom;
    private final BankableRAM ramBanks;

    private MemoryMode mode = MemoryMode._16MBIT_ROM_8KBYTE_RAM;
    private int currentROMBank = 1;

    public MBC1(Memory rom, BankableRAM ramBanks) {
        this.rom = rom;
        this.ramBanks = ramBanks;
    }

    public void step(int cycles) {

    }

    public int readByte(int address) {
        if (MMU.MemoryType.ROM_SWITCHABLE_BANKS.compareTo(address) == 0) {
            return rom.readByte(address + ((currentROMBank - 1) * MMU.MemoryType.ROM_SWITCHABLE_BANKS.size()));
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            return ramBanks.readByte(address - MMU.MemoryType.RAM_BANKS.from);
        }
        return rom.readByte(address);
    }

    public void writeByte(int address, int data) {
        if (address < 0x2000) {
            ramBanks.enable((data & 0b0000_1010) != 0);
        } else if (address < 0x4000) {
            currentROMBank = (currentROMBank & 0b0110_0000) + (data & 0b0001_1111);
            if (currentROMBank == 0 || currentROMBank == 0x20 || currentROMBank == 0x40 || currentROMBank == 0x60) {
                currentROMBank += 1;
            }
        } else if (address < 0x6000) {
            if (mode == MemoryMode._4MBIT_ROM_32KBYTE_RAM) {
                ramBanks.selectBank(data & 0b0000_0011);
            } else {
                data = (data & 0b0000_0011) << 5;
                currentROMBank = (currentROMBank & 0b0001_1111) + data;
            }
        } else if (address < 0x8000) {
            data = data & 0b0000_0001;
            if (data == 1) {
                mode = MemoryMode._4MBIT_ROM_32KBYTE_RAM;
            } else {
                mode = MemoryMode._16MBIT_ROM_8KBYTE_RAM;
            }
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            ramBanks.writeByte(address - MMU.MemoryType.RAM_BANKS.from, data);
        } else {
            log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
        }
    }

    private enum MemoryMode {
        _16MBIT_ROM_8KBYTE_RAM,
        _4MBIT_ROM_32KBYTE_RAM
    }
}
