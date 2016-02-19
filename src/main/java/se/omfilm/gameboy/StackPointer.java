package se.omfilm.gameboy;

public interface StackPointer {
    void write(int value);

    int read();
}
