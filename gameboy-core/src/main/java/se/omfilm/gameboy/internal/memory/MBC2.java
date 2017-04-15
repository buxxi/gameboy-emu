package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.internal.MMU;
import se.omfilm.gameboy.util.DebugPrinter;

public class MBC2 implements Memory {
    private static final Logger log = LoggerFactory.getLogger(MBC2.class);
    private final Memory rom;
    private final BankableRAM builtInRAM; //TODO: should only be 512 bytes
    private int currentROMBank = 1;

    public MBC2(Memory memory, BankableRAM bankableRAM) {
        this.rom = memory;
        this.builtInRAM = bankableRAM;
    }

    public int readByte(int address) {
        if (MMU.MemoryType.ROM_SWITCHABLE_BANKS.compareTo(address) == 0) {
            return rom.readByte(address + ((currentROMBank - 1) * MMU.MemoryType.ROM_SWITCHABLE_BANKS.size()));
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            if (address >= 0xA200) {
                return 0xFF;
            } else {
                return builtInRAM.readByte(address - MMU.MemoryType.RAM_BANKS.from) | 0b1111_0000;
            }
        }
        return rom.readByte(address);
    }

    public void writeByte(int address, int data) {
        if (address < 0x2000) {
            if ((address & 0b0000_0001_0000_0000) == 0) {
                builtInRAM.enable((data & 0b0000_1010) != 0);
            } else {
                log.warn("Trying to enable/disable RAM with invalid address: " + DebugPrinter.hex(address, 4));
            }
        } else if (address >= 0x2000 && address < 0x4000) {
            if ((address & 0b0000_0001_0000_0000) != 0) {
                currentROMBank = (data & 0b0000_1111);
                if (currentROMBank == 0) {
                    currentROMBank = 1;
                }
            } else {
                log.warn("Trying to select a ROM bank with invalid address: " + DebugPrinter.hex(address, 4));
            }
        } else if (MMU.MemoryType.RAM_BANKS.compareTo(address) == 0) {
            if (address >= 0xA200) {
                log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
            } else {
                builtInRAM.writeByte(address - MMU.MemoryType.RAM_BANKS.from, data & 0b0000_1111);
            }
        } else {
            log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
        }
    }
}
