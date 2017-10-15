package se.omfilm.gameboy;

import org.junit.Test;
import se.omfilm.gameboy.BlarggCompabilityReport.ReportName;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CPUInstructionTests extends AbstractSerialBlarggTestRoms {
    @Test(timeout = 2000)
    @ReportName("cpu_instrs/01-special")
    public void itShouldHandleSpecialInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/01-special.gb");
        serial.setExpected("01-special\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/02-interrupts")
    public void itShouldHandleInterruptInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/02-interrupts.gb");
        serial.setExpected("02-interrupts\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/03-op sp,hl")
    public void itShouldHandleSPHLInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/03-op sp,hl.gb");
        serial.setExpected("03-op sp,hl\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/04-op r,imm")
    public void itShouldHandleRIMMInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/04-op r,imm.gb");
        serial.setExpected("04-op r,imm\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/05 op rp")
    public void itShouldHandleRPInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/05-op rp.gb");
        serial.setExpected("05-op rp\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/06-ld r,r")
    public void itShouldHandleLDRRInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/06-ld r,r.gb");
        serial.setExpected("06-ld r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/07-jr,jp,call,ret,rst")
    public void itShouldHandleCallsInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/07-jr,jp,call,ret,rst.gb");
        serial.setExpected("07-jr,jp,call,ret,rst\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/08-misc instrs")
    public void itShouldHandleMiscInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/08-misc instrs.gb");
        serial.setExpected("08-misc instrs\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/09-op r,r")
    public void itShouldHandleRRInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/09-op r,r.gb");
        serial.setExpected("09-op r,r\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/10-bit ops")
    public void itShouldHandleBitInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/10-bit ops.gb");
        serial.setExpected("10-bit ops\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }

    @Test(timeout = 2000)
    @ReportName("cpu_instrs/11-op a,(hl)")
    public void itShouldHandleAHLInstructions() throws IOException, InterruptedException {
        loadROM("cpu_instrs.zip", "cpu_instrs/individual/11-op a,(hl).gb");
        serial.setExpected("11-op a,(hl)\n\n\nPassed\n");

        target.run();

        assertEquals(serial.expected, serial.result);
    }
}
