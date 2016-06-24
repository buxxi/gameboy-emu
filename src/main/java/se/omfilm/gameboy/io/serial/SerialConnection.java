package se.omfilm.gameboy.io.serial;

/**
 * Data is written/read from this to emulate the serial port on the gameboy.
 * Mostly used to output data from the unit tests.
 */
public interface SerialConnection {
    void data(int data);

    void control(int control);

    int data();

    int control();
}
