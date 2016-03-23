package se.omfilm.gameboy;

public interface SerialConnection {
    void setData(int data);

    void setControl(int control);

    int getData();

    int getControl();
}
