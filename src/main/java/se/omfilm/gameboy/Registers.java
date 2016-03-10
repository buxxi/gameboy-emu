package se.omfilm.gameboy;

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
