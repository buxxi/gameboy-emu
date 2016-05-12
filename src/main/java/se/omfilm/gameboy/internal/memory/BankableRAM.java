package se.omfilm.gameboy.internal.memory;

public class BankableRAM implements Memory {
    private final Memory[] banks;

    private boolean enabled = false;
    private int currentBank = 0;

    public BankableRAM(int banks, int size) {
        this.banks = new Memory[banks];
        for (int i = 0; i < banks; i++) {
            this.banks[i] = new ByteArrayMemory(new byte[size]);
        }
    }

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
}
