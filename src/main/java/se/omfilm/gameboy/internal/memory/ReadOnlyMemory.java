package se.omfilm.gameboy.internal.memory;

public class ReadOnlyMemory implements Memory {
    private final Memory delegate;

    public ReadOnlyMemory(Memory delegate) {
        this.delegate = delegate;
    }

    public int readByte(int address) {
        return delegate.readByte(address);
    }

    public void writeByte(int address, int data) {
        throw new UnsupportedOperationException("Can't write to " + ReadOnlyMemory.class.getName());
    }
}
