package se.omfilm.gameboy;

import se.omfilm.gameboy.instructions.*;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.function.Supplier;

public interface Instruction {
    /**
     * Execute the instruction and return the number of cycles that instruction took
     */
    int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer);

    interface RegisterWriter {
        void write(Registers registers, int value);
    }

    interface RegisterReader {
        int read(Registers registers);
    }

    /**
     * See this file for reference: http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
     */
    enum InstructionType {
        NOP(        0x00, NoOp::new), //Page 97
        LD_BC_nn(   0x01, LoadWordIntoRegister::toBC), //Page 76
        LD_BC_A(    0x02, LoadRegisterIntoAddressOfWordRegister::AtoBC), //Page 69
        INC_BC(     0x03, IncrementWordRegister::BC), //Page 92
        INC_B(      0x04, IncrementByteRegister::B), //Page 88
        DEC_B(      0x05, DecrementByteRegister::B), //Page 89
        LD_B_n(     0x06, LoadByteIntoRegister::toB), //Page 65
        LD_nn_SP(   0x08, LoadStackPointerToAddressOfWord::new), //Page 78
        ADD_HL_BC(  0x09, AddWordRegisterIntoRegister::BCtoHL), //Page 90
        DEC_BC(     0x0B, DecrementWordRegister::BC), //Page 93
        INC_C(      0x0C, IncrementByteRegister::C), //Page 88
        DEC_C(      0x0D, DecrementByteRegister::C), //Page 89
        LD_C_n(     0x0E, LoadByteIntoRegister::toC), //Page 65

        STOP(       0x10, InvalidInstruction::new), //Page 97
        LD_DE_nn(   0x11, LoadWordIntoRegister::toDE), //Page 76
        LD_DE_A(    0x12, LoadRegisterIntoAddressOfWordRegister::AtoDE), //Page 69
        INC_DE(     0x13, IncrementWordRegister::DE), //Page 92
        INC_D(      0x14, IncrementByteRegister::D), //Page 88
        DEC_D(      0x15, DecrementByteRegister::D), //Page 89
        LD_D_n(     0x16, LoadByteIntoRegister::toD), //Page 65
        RLA(        0x17, RotateRegisterLeft::A), //Page 99
        JR_n(       0x18, JumpRelative::unconditional), //Page 112
        ADD_HL_DE(  0x19, AddWordRegisterIntoRegister::DEtoHL), //Page 90
        LD_A_DE(    0x1A, LoadByteAddressOfRegisterIntoRegister::fromDEtoA), //Page 68
        INC_E(      0x1C, IncrementByteRegister::E), //Page 88
        DEC_E(      0x1D, DecrementByteRegister::E), //Page 89
        LD_E_n(     0x1E, LoadByteIntoRegister::toE), //Page 65
        RRA(        0x1F, RotateRegisterRight::A), //Page 100

        JR_NZ_n(    0x20, JumpRelative::ifLastNotZero), //Page 113
        LD_HL_nn(   0x21, LoadWordIntoRegister::toHL), //Page 76
        LDI_HL_A(   0x22, LoadAIntoAddressOfHLIncreased::new), //Page 74
        INC_HL(     0x23, IncrementWordRegister::HL), //Page 92,
        INC_H(      0x24, IncrementByteRegister::H), //Page 88
        DEC_H(      0x25, DecrementByteRegister::H), //Page 89
        LD_H_n(     0x26, LoadByteIntoRegister::toH), //Page 65
        JR_Z_n(     0x28, JumpRelative::ifLastZero), //Page 113
        ADD_HL_HL(  0x29, AddWordRegisterIntoRegister::HLtoHL), //Page 90
        LDI_A_HL(   0x2A, LoadAddressOfHLIncreasedIntoA::new), //Page 73
        INC_L(      0x2C, IncrementByteRegister::L), //Page 88
        DEC_L(      0x2D, DecrementByteRegister::L), //Page 89
        LD_L_n(     0x2E, LoadByteIntoRegister::toL), //Page 65
        CPL(        0x2F, ComplementA::new), //Page 95

        JR_NC_n(    0x30, JumpRelative::ifLastNotCarry), //Page 113
        LD_SP_nn(   0x31, LoadWordIntoSP::new), //Page 76
        LDD_HL_A(   0x32, LoadAIntoAddressOfHLDecreased::new), //Page 72
        INC_HL_n(   0x34, IncrementByteAddressOfHL::new), //Page 88
        DEC_HL_n(   0x35, DecrementAddressOfHL::new), //Page 89
        LD_HL_n(    0x36, LoadByteIntoAddressOfHL::new), //Page 67
        INC_A(      0x3C, IncrementByteRegister::A), //Page 88
        DEC_A(      0x3D, DecrementByteRegister::A), //Page 89
        LD_A_n(     0x3E, LoadByteIntoRegister::toA), //Page 68

        LD_B_B(     0x40, LoadRegisterIntoRegister::fromBtoB), //Page 66
        LD_B_HL(    0x46, LoadByteAddressOfRegisterIntoRegister::fromHLtoB), //Page 66
        LD_B_A(     0x47, LoadRegisterIntoRegister::fromAToB), //Page 69
        LD_C_HL(    0x4E, LoadByteAddressOfRegisterIntoRegister::fromHLtoC), //Page 66
        LD_C_A(     0x4F, LoadRegisterIntoRegister::fromAToC), //Page 69

        LD_D_H(     0x54, LoadRegisterIntoRegister::fromHtoD), //Page 69
        LD_D_HL(    0x56, LoadByteAddressOfRegisterIntoRegister::fromHLtoD), //Page 66
        LD_D_A(     0x57, LoadRegisterIntoRegister::fromAToD), //Page 69
        LD_E_L(     0x5D, LoadRegisterIntoRegister::fromLtoE), //Page 69
        LD_E_HL(    0x5E, LoadByteAddressOfRegisterIntoRegister::fromHLtoE), //Page 66
        LD_E_A(     0x5F, LoadRegisterIntoRegister::fromAToE), //Page 69

        LD_H_A(     0x67, LoadRegisterIntoRegister::fromAToH), //Page 69
        LD_L_HL(    0x6E, LoadByteAddressOfRegisterIntoRegister::fromHLtoL), //Page 66
        LD_L_A(     0x6F, LoadRegisterIntoRegister::fromAToL), //Page 69

        LD_HL_B(    0x70, LoadRegisterIntoAddressOfWordRegister::BtoHL), //Page 67
        LD_HL_C(    0x71, LoadRegisterIntoAddressOfWordRegister::CtoHL), //Page 67
        LD_HL_D(    0x72, LoadRegisterIntoAddressOfWordRegister::DtoHL), //Page 67
        LD_HL_E(    0x73, LoadRegisterIntoAddressOfWordRegister::EtoHL), //Page 67
        LD_HL_A(    0x77, LoadRegisterIntoAddressOfWordRegister::AtoHL), //Page 69
        LD_A_B(     0x78, LoadRegisterIntoRegister::fromBToA), //Page 66
        LD_A_C(     0x79, LoadRegisterIntoRegister::fromCToA), //Page 66
        LD_A_D(     0x7A, LoadRegisterIntoRegister::fromDtoA), //Page 66
        LD_A_E(     0x7B, LoadRegisterIntoRegister::fromEToA), //Page 66
        LD_A_H(     0x7C, LoadRegisterIntoRegister::fromHToA), //Page 66
        LD_A_L(     0x7D, LoadRegisterIntoRegister::fromLToA), //Page 66
        LD_A_HL(    0x7E, LoadByteAddressOfRegisterIntoRegister::fromHLtoA), //Page 66

        ADD_A_C(    0x81, AddRegisterIntoA::C), //Page 80
        ADD_A_HL(   0x86, AddByteAddressOfHLIntoA::new), //Page 80
        ADD_A_A(    0x87, AddRegisterIntoA::A), //Page 80
        ADC_A_E(    0x8B, AddRegisterWithCarryIntoA::E), //Page 81

        SUB_B(      0x90, SubtractRegisterFromA::fromB), //Page 82
        SUB_C(      0x91, SubtractRegisterFromA::fromC), //Page 82
        SUB_HL(     0x96, SubtractAddressOfHLFromA::new), //page 96
        SUB_A(      0x97, SubtractRegisterFromA::fromA), //Page 82

        AND_C(      0xA1, AndRegisterWithA::C), //Page 84
        AND_A(      0xA7, AndRegisterWithA::A), //Page 84
        XOR_C(      0xA9, XorRegisterWithA::C), //Page 86
        XOR_L(      0xAD, XorRegisterWithA::L), //Page 86
        XOR_HL(     0xAE, XorAddressOfHLWithA::new), //Page 86
        XOR_A(      0xAF, XorRegisterWithA::A), //Page 86

        OR_B(       0xB0, OrRegisterWithA::B), //Page 85
        OR_C(       0xB1, OrRegisterWithA::C), //Page 85
        OR_HL(      0xB6, OrAddressOfHLWithA::new), //Page 85
        OR_A(       0xB7, OrRegisterWithA::A), //Page 85
        CP_HL(      0xBE, CompareByteAddressOfHLAgainstA::new), //Page 87

        RET_NZ(     0xC0, Return::ifNotZero), //Page 117
        POP_BC(     0xC1, PopStackIntoRegister::toBC), //Page 79
        JP_NZ(      0xC2, JumpWord::ifLastNotZero), //Page 111
        JP_nn(      0xC3, JumpWord::unconditional), //Page 111
        CALL_NZ(    0xC4, CallRoutine::ifLastNotZero), //Page 115
        PUSH_BC(    0xC5, PushRegisterIntoStack::fromBC), //Page 78
        ADD_n(      0xC6, AddByteFromAddressIntoA::new), //Page 80
        RET_Z(      0xC8, Return::ifZero), //Page 117
        RET(        0xC9, Return::unconditional), //Page 117
        CB(         0xCB, InvalidInstruction::new), //Page 99-110, special case (append 0xCB before CB-instructions)
        CALL_nn(    0xCD, CallRoutine::unconditional), //Page 114
        ADC_A_n(    0xCE, AddByteFromAddressWithCarryIntoA::new), //Page 81

        RET_NC(     0xD0, Return::ifNotCarry), //Page 117
        POP_DE(     0xD1, PopStackIntoRegister::toDE), //Page 79
        PUSH_DE(    0xD5, PushRegisterIntoStack::fromDE), //Page 78
        SUB_n(      0xD6, SubtractByteFromA::new), //Page 82
        RET_C(      0xD8, Return::ifCarry), //Page 117
        RETI(       0xD9, Return::andEnableInterrupts), //Page 118

        LDH_n_A(    0xE0, LoadAOffsetByte::new), //Page 75
        POP_HL(     0xE1, PopStackIntoRegister::toHL), //Page 79
        LDH_C_A(    0xE2, LoadAOffsetC::new), //Page 70
        PUSH_HL(    0xE5, PushRegisterIntoStack::fromHL), //Page 78
        AND_n(      0xE6, AndByteWithA::new), //Page 84
        JP_HL(      0xE9, JumpToValueOfHL::new), //Page 112
        LD_nn_A(    0xEA, LoadAIntoAddressOfWord::new), //Page 69
        XOR_n(      0xEE, XorByteWithA::new), //Page 86
        RST_28(     0xEF, Restart::to28), //Page 116

        LDH_A_n(    0xF0, LoadByteOffsetIntoA::new), //Page 75
        POP_AF(     0xF1, PopStackIntoRegister::toAF), //Page 79
        DI(         0xF3, DisableInterrupts::new), //Page 98
        PUSH_AF(    0xF5, PushRegisterIntoStack::fromAF), //Page 78
        OR_n(       0xF6, OrByteWithA::new), //Page 85
        LD_A_nn(    0xFA, LoadAddressIntoA::new), //Page 68
        EI(         0xFB, EnableInterrupts::new), //Page 98
        CP_n(       0xFE, CompareByteAgainstA::new), //Page 87
        RST_38(     0xFF, Restart::to38), //Page 116

        RL_C(       0xCB11, RotateRegisterLeft::C), //Page 102
        RR_C(       0xCB19, RotateRegisterRight::C), //Page 104
        RR_D(       0xCB1A, RotateRegisterRight::D), //Page 104
        RR_E(       0xCB1B, RotateRegisterRight::E), //Page 104
        SWAP_A(     0xCB37, SwapRegister::A), //Page 94
        SRL_B(      0xCB38, ShiftRegisterRight::B), //Page 107
        SRL_A(      0xCB3F, ShiftRegisterRight::A), //Page 107
        BIT_7H(     0xCB7C, CompareBit7::new), //Page 108
        RES_A0(     0xCB87, ResetBitInRegister::bit0InA); //Page 110

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
            for (InstructionType instructionType : InstructionType.values()) {
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
}
