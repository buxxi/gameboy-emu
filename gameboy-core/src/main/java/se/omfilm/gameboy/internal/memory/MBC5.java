package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.MMU;
import se.omfilm.gameboy.util.DebugPrinter;

public class MBC5 implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MBC5.class);

    private final Memory rom;
    private final BankableRAM ramBanks;

    private int currentROMBank = 1;

    public MBC5(Memory rom, BankableRAM ramBanks) {
        this.rom = rom;
        this.ramBanks = ramBanks;
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
        } else if (address < 0x3000) {
            currentROMBank = (currentROMBank & 0b0000_0001_0000_0000) | data;
        } else if (address < 0x4000) {
            currentROMBank = (currentROMBank & 0b0000_0000_1111_1111) | ((data & 0b0000_0001) << 8);
        } else if (address < 0x6000) {
            ramBanks.selectBank(data & 0b0000_0011);
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            ramBanks.writeByte(address - MMU.MemoryType.RAM_BANKS.from, data);
        } else {
            log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
        }
    }
}
