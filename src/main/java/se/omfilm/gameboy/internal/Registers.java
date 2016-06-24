package se.omfilm.gameboy.internal;

/**
 * Represents the different registers for the CPU with a read and write method for each.
 *
 * Some of them are grouped together for easier usage in the instructions (AF, BC, DE, HL).
 * All the grouped registers should return an unsigned integer between 0x0000 - 0xFFFF where the most significant bytes is the first register.
 * The single registers should return an unsigned integer between 0x00 - 0xFF.
 *
 * Register F is for the flags of the CPU which can also be obtained from the Flags-interface.
 *
 * TODO: make default methods for grouped registers instead of implementing them in the CPU
 */
public interface Registers {
    int readH();

    void writeH(int val);

    int readL();

    void writeL(int val);

    int readHL();

    void writeHL(int val);

    int readDE();

    void writeDE(int val);

    int readA();

    void writeA(int val);

    int readC();

    void writeC(int val);

    int readB();

    void writeB(int val);

    int readBC();

    void writeBC(int val);

    int readD();

    void writeD(int val);

    int readE();

    void writeE(int val);

    int readF();

    void writeF(int val);

    int readAF();

    void writeAF(int val);
}
