package se.omfilm.gameboy.internal.memory;

public class MBC1 implements Memory {
    private final Memory rom;

    private int currentBank = 1;

    public MBC1(Memory rom) {
        this.rom = rom;
    }

    public int readByte(int address) {
        if (address >= MemoryType.ROM_SWITCHABLE_BANKS.from) {
            return rom.readByte(address + ((currentBank - 1) * MemoryType.ROM_SWITCHABLE_BANKS.size()));
        }
        return rom.readByte(address);
    }

    public void writeByte(int address, int data) {
        if (address >= 0x2000 && address <= MemoryType.ROM_BANK0.to) {
            currentBank = (data & 0b0001_1111);
            if (currentBank == 0) {
                currentBank = 1;
            }
        } else {
            throw new IllegalArgumentException("Can't write to " + getClass().getSimpleName());
        }
    }
}
