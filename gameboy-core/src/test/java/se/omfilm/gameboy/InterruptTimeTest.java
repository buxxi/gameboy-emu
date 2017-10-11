package se.omfilm.gameboy;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class InterruptTimeTest extends AbstractMemoryBlarggTestRoms {
    @Test(timeout = 2000)
    public void itShouldHandleInterruptTiming() throws IOException, InterruptedException {
        loadROM("interrupt_time.zip", "interrupt_time/interrupt_time.gb");

        target.run();

        assertEquals("interrupt time\n\n\nPassed\n", target.result());
    }
}
