package se.omfilm.gameboy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(2)
public class CPUInstructionTests extends AbstractSerialBlarggTestRoms {
    @Test
    @ReportName("cpu_instrs/01-special")
    void itShouldHandleSpecialInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/01-special.gb");

        target.run();

        assertEquals("01-special\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/02-interrupts")
    void itShouldHandleInterruptInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/02-interrupts.gb");

        target.run();

        assertEquals("02-interrupts\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/03-op sp,hl")
    void itShouldHandleSPHLInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/03-op sp,hl.gb");

        target.run();

        assertEquals("03-op sp,hl\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/04-op r,imm")
    void itShouldHandleRIMMInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/04-op r,imm.gb");

        target.run();

        assertEquals("04-op r,imm\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/05 op rp")
    void itShouldHandleRPInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/05-op rp.gb");

        target.run();

        assertEquals("05-op rp\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/06-ld r,r")
    void itShouldHandleLDRRInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/06-ld r,r.gb");

        target.run();

        assertEquals("06-ld r,r\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/07-jr,jp,call,ret,rst")
    void itShouldHandleCallsInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");

        target.run();

        assertEquals("07-jr,jp,call,ret,rst\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/08-misc instrs")
    void itShouldHandleMiscInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/08-misc instrs.gb");

        target.run();

        assertEquals("08-misc instrs\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/09-op r,r")
    void itShouldHandleRRInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/09-op r,r.gb");

        target.run();

        assertEquals("09-op r,r\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/10-bit ops")
    void itShouldHandleBitInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/10-bit ops.gb");

        target.run();

        assertEquals("10-bit ops\n\n\nPassed\n", serial.result);
    }

    @Test
    @ReportName("cpu_instrs/11-op a,(hl)")
    void itShouldHandleAHLInstructions() throws IOException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/11-op a,(hl).gb");
 
        target.run();

        assertEquals("11-op a,(hl)\n\n\nPassed\n", serial.result);
    }
}
