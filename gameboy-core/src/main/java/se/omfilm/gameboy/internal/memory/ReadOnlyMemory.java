package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class ReadOnlyMemory implements Memory {
    private static final Logger log = LoggerFactory.getLogger(ReadOnlyMemory.class);

    private final Memory delegate;

    public ReadOnlyMemory(Memory delegate) {
        this.delegate = delegate;
    }

    public int readByte(int address) {
        return delegate.readByte(address);
    }

    public void writeByte(int address, int data) {
        log.warn("Can't write " + DebugPrinter.hex(data, 2) + " to " + getClass().getSimpleName() + "@" + DebugPrinter.hex(address, 4));
    }
}
