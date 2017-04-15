package se.omfilm.gameboy.io.serial;

public class NullSerialConnection implements SerialConnection {
    private int data;
    private int control;

    public void data(int data) {
        this.data = data;
    }

    public void control(int control) {
        this.control = control;
    }

    public int data() {
        return data;
    }

    public int control() {
        return control;
    }
}
