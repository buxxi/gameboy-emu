package se.omfilm.gameboy.internal;

/**
 * Represents the different registers for the CPU with a read and write method for each.
 * <p>
 * Some of them are grouped together for easier usage in the instructions (AF, BC, DE, HL).
 * All the grouped registers should return an unsigned integer between 0x0000 - 0xFFFF where the most significant bytes is the first register.
 * The single registers should return an unsigned integer between 0x00 - 0xFF.
 * <p>
 * Register F is for the flags of the CPU which can also be obtained from the Flags-interface.
 */
public interface Registers {
    int readA();

    void writeA(int val);

    int readB();

    void writeB(int val);

    int readC();

    void writeC(int val);

    int readD();

    void writeD(int val);

    int readE();

    void writeE(int val);

    int readF();

    void writeF(int val);

    int readH();

    void writeH(int val);

    int readL();

    void writeL(int val);

    default int readAF() {
        return readA() << 8 | readF();
    }

    default void writeAF(int val) {
        writeA(val >> 8);
        writeF(val & 0x00FF);
    }

    default int readBC() {
        return readB() << 8 | readC();
    }

    default void writeBC(int val) {
        writeB(val >> 8);
        writeC(val & 0x00FF);
    }

    default int readDE() {
        return readD() << 8 | readE();
    }

    default void writeDE(int val) {
        writeD(val >> 8);
        writeE(val & 0x00FF);
    }

    default int readHL() {
        return readH() << 8 | readL();
    }

    default void writeHL(int val) {
        writeH(val >> 8);
        writeL(val & 0x00FF);
    }
}
