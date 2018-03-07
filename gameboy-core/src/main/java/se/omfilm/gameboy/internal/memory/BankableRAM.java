package se.omfilm.gameboy.internal.memory;

import se.omfilm.gameboy.internal.MMU;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public abstract class BankableRAM implements Memory {
    private final Memory[] banks;

    private boolean enabled = false;
    private int currentBank = 0;

    private BankableRAM(int banks) {
        this.banks = new Memory[banks];
        for (int i = 0; i < banks; i++) {
            this.banks[i] = createBank(i);
        }
    }

    protected abstract Memory createBank(int bank);

    public abstract Memory clockData(int size);

    public int readByte(int address) {
        if (!enabled) {
            return 0;
        }
        return banks[currentBank].readByte(address);
    }

    public void writeByte(int address, int data) {
        if (!enabled) {
            return;
        }
        banks[currentBank].writeByte(address, data);
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    public void selectBank(int bank) {
        this.currentBank = bank;
    }

    public static BankableRAM inMemory(int banks) {
        return new BankableRAM(banks) {
            protected Memory createBank(int bank) {
                return new ByteArrayMemory(MMU.MemoryType.RAM_BANKS.allocate());
            }

            public Memory clockData(int size) {
                return new ByteArrayMemory(new byte[size]);
            }
        };
    }

    public static BankableRAM toFile(int banks, File file) throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        return new BankableRAM(banks) {
            protected Memory createBank(int bank) {
                return new PersistentMemory(bank * MMU.MemoryType.RAM_BANKS.size(), raf);
            }

            public Memory clockData(int size) {
                return new PersistentMemory(banks * MMU.MemoryType.RAM_BANKS.size(), raf);
            }
        };
    }
}
