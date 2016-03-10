package se.omfilm.gameboy.instructions;

import se.omfilm.gameboy.*;

public class ReturnEnableInterrupts extends Return implements DelayedInstruction {
    public boolean disableInterrupts() {
        return false;
    }
}
