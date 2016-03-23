package se.omfilm.gameboy.io.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.SerialConnection;


public class ConsoleSerialConnection implements SerialConnection {
    private static final Logger log = LoggerFactory.getLogger(ConsoleSerialConnection.class);

    private int data;
    private int control;

    public void setData(int data) {
        this.data = data;
    }

    public void setControl(int control) {
        this.control = control;
        if ((control & 0b1000_0000) != 0) {
            System.out.print((char) this.data);
        }
    }

    public int getData() {
        return data;
    }

    public int getControl() {
        return control;
    }
}
