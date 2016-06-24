package se.omfilm.gameboy.internal.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

//TODO: this keeps the file open the entire time, is that ok?
public class PersistentMemory implements Memory {
    private static final Logger log = LoggerFactory.getLogger(PersistentMemory.class);

    private final int offset;
    private final RandomAccessFile file;

    public PersistentMemory(int offset, RandomAccessFile file) {
        this.offset = offset;
        this.file = file;
    }

    public int readByte(int address) {
        try {
            file.seek(offset + address);
            return unsigned(file.readByte());
        }
        catch (EOFException ignored) {}
        catch (IOException e) {
            log.warn("Could not read byte for address " + address);
        }
        return 0xFF;
    }

    public void writeByte(int address, int data) {
        try {
            file.seek(offset + address);
            file.writeByte(signed(data));
        } catch (IOException e) {
            log.warn("Could not write byte for address " + address);
        }
    }

    private int unsigned(byte input) {
        return input & 0xFF;
    }

    private byte signed(int input) {
        return (byte) input;
    }
}
