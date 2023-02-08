package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(2)
public class InterruptTimeTest extends AbstractMemoryBlarggTestRoms {
    @Test
    @ReportName("interrupt_time/interrupt time")
    void itShouldHandleInterruptTiming() throws IOException {
        loadROM("interrupt_time.zip", "interrupt_time/interrupt_time.gb");

        target.run();

        assertEquals("interrupt time\n\n\nPassed\n", target.result());
    }
}
