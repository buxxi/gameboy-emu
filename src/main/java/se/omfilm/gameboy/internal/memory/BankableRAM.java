package se.omfilm.gameboy.internal.memory;

public class BankableRAM implements Memory {
    private final Memory[] banks;

    public BankableRAM(int banks, int size) {
        this.banks = new Memory[banks];
        for (int i = 0; i < banks; i++) {
            this.banks[i] = new ByteArrayMemory(new byte[size]);
        }
    }

    public int readByte(int address) {
        return banks[0].readByte(address);
    }

    public void writeByte(int address, int data) {
        banks[0].writeByte(address, data);
    }
}
