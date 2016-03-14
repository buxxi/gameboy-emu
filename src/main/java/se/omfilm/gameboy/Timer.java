package se.omfilm.gameboy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.util.DebugPrinter;

public class Timer {
    private static final Logger log = LoggerFactory.getLogger(Timer.class);

    private final Flags flags;

    public Timer(Flags flags) {
        this.flags = flags;
    }

    public void setModulo(int data) {
        log.warn("Timer modulo set to " + DebugPrinter.hex(data, 4) + " but unhandled");
    }

    public void setControl(int data) {
        log.warn("Timer control set to " + DebugPrinter.hex(data, 4) + " but unhandled");
    }
}
