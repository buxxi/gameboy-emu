package se.omfilm.gameboy.io.serial;

public interface SerialConnection {
    void setData(int data);

    void setControl(int control);

    int getData();

    int getControl();
}
