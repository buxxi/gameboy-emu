package se.omfilm.gameboy;

public interface StackPointer {
    void write(int value);

    int read();

    default void decreaseWord() {
        write(read() - 2);
    }

    default void increaseWord() {
        write(read() + 2);
    }
}
