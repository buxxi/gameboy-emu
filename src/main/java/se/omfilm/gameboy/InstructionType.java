package se.omfilm.gameboy;

import se.omfilm.gameboy.instructions.*;

import java.util.function.Supplier;

/**
 * See this file for reference: http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
 */
public enum InstructionType {
    INC_B(      0x04, IncrementByteRegister::B), //Page 88
    DEC_B(      0x05, DecrementByteRegister::B), //Page 89
    LD_B_n(     0x06, LoadByteIntoRegister::toB), //Page 65
    INC_C(      0x0C, IncrementByteRegister::C), //Page 88
    DEC_C(      0x0D, DecrementByteRegister::C), //Page 89
    LD_C_n(     0x0E, LoadByteIntoRegister::toC), //Page 65

    LD_DE_nn(   0x11, LoadWordIntoRegister::toDE), //Page 76
    INC_DE(     0x13, IncrementWordRegister::DE), //Page 92
    DEC_D(      0x15, DecrementByteRegister::D), //Page 89
    LD_D_n(     0x16, LoadByteIntoRegister::toD), //Page 65
    RLA(        0x17, RotateRegisterLeft::A), //Page 99
    JR_n(       0x18, JumpRelative::new), //Page 112
    LD_A_DE(    0x1A, LoadByteAddressOfDEIntoA::new), //Page 68
    DEC_E(      0x1D, DecrementByteRegister::E), //Page 89
    LD_E_n(     0x1E, LoadByteIntoRegister::toE), //Page 65

    JR_NZ_n(    0x20, JumpRelativeIfLastNotZero::new), //Page 113
    LD_HL_nn(   0x21, LoadWordIntoRegister::toHL), //Page 76
    LDI_HL_A(   0x22, LoadAIntoAddressOfHLIncreased::new), //Page 74
    INC_HL(     0x23, IncrementWordRegister::HL), //Page 92,
    INC_H(      0x24, IncrementByteRegister::H), //Page 88
    JR_Z_n(     0x28, JumpRelativeIfLastZero::new), //Page 113
    LD_L_n(     0x2E, LoadByteIntoRegister::toL), //Page 65

    LD_SP_nn(   0x31, LoadWordIntoSP::new), //Page 76
    LDD_HL_A(   0x32, LoadHLDecreaseIntoA::new), //Page 72
    DEC_A(      0x3D, DecrementByteRegister::A), //Page 89
    LD_A_n(     0x3E, LoadByteIntoRegister::toA), //Page 68

    LD_C_A(     0x4F, LoadRegisterIntoRegister::fromAToC), //Page 69

    LD_D_A(     0x57, LoadRegisterIntoRegister::fromAToD), //Page 69

    LD_H_A(     0x67, LoadRegisterIntoRegister::fromAToH), //Page 69

    LD_HL_A(    0x77, LoadAIntoAddressOfHL::new), //Page 69
    LD_A_B(     0x78, LoadRegisterIntoRegister::fromBToA), //Page 66
    LD_A_E(     0x7B, LoadRegisterIntoRegister::fromEToA), //Page 66
    LD_A_H(     0x7C, LoadRegisterIntoRegister::fromHToA), //Page 66
    LD_A_L(     0x7D, LoadRegisterIntoRegister::fromLToA), //Page 66

    ADD_A_HL(   0x86, AddByteAddressOfHLIntoA::new), //Page 80

    SUB_A_B(    0x90, SubtractRegisterFromA::fromB), //Page 82

    XOR_A(      0xAF, XorA::new), //Page 86

    CP_HL(      0xBE, CompareByteAddressOfHLAgainstA::new), //Page 87

    POP_BC(     0xC1, PopStackIntoRegister::toBC), //Page 79
    PUSH_BC(    0xC5, PushRegisterIntoStack::fromBC), //Page 78
    RET(        0xC9, Return::new), //Page 117
    CB(         0xCB, InvalidInstruction::new), //Page 99-110, special case (append 0xCB before CB-instructions)
    CALL_nn(    0xCD, CallRoutineImmediate::new), //Page 114

    LDH_n_A(    0xE0, LoadAOffsetByte::new), //Page 75
    LDH_C_A(    0xE2, LoadAOffsetC::new), //Page 70,
    LD_nn_A(    0xEA, LoadAIntoAddressOfWord::new), //Page 69

    LDH_A_n(    0xF0, LoadByteOffsetIntoA::new), //Page 75
    CP_n(       0xFE, CompareByteAgainstA::new), //Page 87

    CB_RL_C(    0xCB11, RotateRegisterLeft::C), //Page 102
    CB_BIT_7H(  0xCB7C, CompareBit7::new); //Page 108

    private final int opcode;
    private final Supplier<Instruction> instructionSupplier;

    InstructionType(int opcode, Supplier<Instruction> instructionSupplier) {
        this.opcode = opcode;
        this.instructionSupplier = instructionSupplier;
    }

    public static InstructionType fromOpCode(int prefix, int opcode) {
        opcode = (prefix << 8) | opcode;
        return fromOpCode(opcode);
    }

    public static InstructionType fromOpCode(int opcode) {
        for (InstructionType instructionType : values()) {
            if (instructionType.opcode == opcode) {
                return instructionType;
            }
        }
        throw new IllegalArgumentException("No such instruction " + DebugPrinter.hex(opcode, 4));
    }

    @Override
    public String toString() {
        return DebugPrinter.hex(this.opcode, 4) + " (" + super.toString() + ")";
    }

    public Supplier<Instruction> instruction() {
        return instructionSupplier;
    }

    public int opcode() {
        return opcode;
    }
}
