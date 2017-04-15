package se.omfilm.gameboy.io.serial;

/**
 * Prints the data written to the serial connection to the console.
 * Made for Blargg's test roms.
 */
public class ConsoleSerialConnection implements SerialConnection {
    private int data;
    private int control;

    public void data(int data) {
        this.data = data;
    }

    public void control(int control) {
        this.control = control;
        if ((control & 0b1000_0000) != 0) {
            System.out.print((char) this.data);
        }
    }

    public int data() {
        return data;
    }

    public int control() {
        return control;
    }
}
