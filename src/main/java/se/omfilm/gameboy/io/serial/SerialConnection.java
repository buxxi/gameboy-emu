package se.omfilm.gameboy.io.serial;

public interface SerialConnection {
    void data(int data);

    void control(int control);

    int data();

    int control();
}
