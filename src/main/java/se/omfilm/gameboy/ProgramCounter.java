package se.omfilm.gameboy;

public interface ProgramCounter {
    int increase();

    int increase(int amount);

    int read();

    void write(int data);
}
