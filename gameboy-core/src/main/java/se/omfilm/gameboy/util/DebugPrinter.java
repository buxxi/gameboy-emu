package se.omfilm.gameboy.util;

public class DebugPrinter {
    public static String hex(int val, int length) {
        if (length == 2) {
            return String.format("0x%02X", val);
        } else {
            return String.format("0x%04X", val);
        }
    }
}
