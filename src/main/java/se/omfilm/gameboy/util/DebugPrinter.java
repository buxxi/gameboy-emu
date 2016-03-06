package se.omfilm.gameboy.util;

import jdk.nashorn.internal.runtime.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.*;

public class DebugPrinter {
    private static final Logger log = LoggerFactory.getLogger(DebugPrinter.class);

    public static void debug(StackPointer stackPointer, ProgramCounter programCounter) {
        log.debug(
            "PC:\t" + hex(programCounter.read(), 4) + "\tSP:\t" + hex(stackPointer.read(), 4)
        );
    }

    public static void debug(Registers registers) {
        log.debug(
            "A:\t" + hex(registers.readA(), 2) + "\tF:\t" + hex(registers.readF(), 2) + "\tAF:\t" + hex(registers.readAF(), 4) + "\n" +
            "B:\t" + hex(registers.readB(), 2) + "\tC:\t" + hex(registers.readC(), 2) + "\tBC:\t" + hex(registers.readBC(), 4) + "\n" +
            "H:\t" + hex(registers.readH(), 2) + "\tL:\t" + hex(registers.readL(), 2) + "\tHL:\t" + hex(registers.readHL(), 4) + "\n" +
            "D:\t" + hex(registers.readD(), 2) + "\tD:\t" + hex(registers.readE(), 2) + "\tDE:\t" + hex(registers.readDE(), 4) + "\n"
        );
    }

    public static String hex(int val, int length) {
        String result = Integer.toHexString(val).toUpperCase();
        while (result.length() < length) {
            result = "0" + result;
        }
        return "0x" + result;
    }

    public static void debugException(Exception e) throws InterruptedException {
        Thread.sleep(100);
        e.printStackTrace();
        log.error(Instruction.InstructionType.values().length + " instructions implemented of 512");
        System.exit(0);
    }

    public static void verifyBoot(CPU cpu, StackPointer stackPointer) {
        verify("AF", 0x01B0, cpu.readAF());
        verify("BC", 0x0013, cpu.readBC());
        verify("DE", 0x00D8, cpu.readDE());
        verify("HL", 0x014D, cpu.readHL());
        verify("SP", 0xFFFE, stackPointer.read());
    }

    private static void verify(String name, int expected, int got) {
        if (got != expected) {
            throw new IllegalStateException("Register " + name + " has wrong value after boot, expected: " + hex(expected, 4) + " but got: " + hex(got, 4));
        }
    }
}
