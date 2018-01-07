package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.instructions.*;
import se.omfilm.gameboy.internal.instructions.arithmetic.*;
import se.omfilm.gameboy.internal.instructions.bitmanipulation.*;
import se.omfilm.gameboy.internal.instructions.flagmanipulation.*;
import se.omfilm.gameboy.internal.instructions.load.*;
import se.omfilm.gameboy.internal.instructions.logic.*;
import se.omfilm.gameboy.internal.instructions.stack.*;
import se.omfilm.gameboy.internal.memory.Memory;
import se.omfilm.gameboy.util.DebugPrinter;
import se.omfilm.gameboy.util.EnumByValue;

import java.util.function.Supplier;

/**
 * A common interface for all the instructions the CPU can execute.
 * The CPU keeps a cache of these for each InstructionType so they should all be stateless.
 *
 * This is made as a functional interface so it can be used as a lambda for simple instructions.
 */
@FunctionalInterface
public interface Instruction {
    default int paddingCycles() {
        return 0;
    }

    /**
     * Execute the instruction and return the number of cycles that instruction took.
     * This should NEVER throw an Exception, so that needs to be handled in the the instruction.
     *
     * The lowest value that it can return is 4
     */
    int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer);

    /**
     * An interface that is mostly used to be able to use a single register as method reference.
     * This is for instructions that does the same thing to different registers for different opcodes.
     */
    @FunctionalInterface
    interface RegisterWriter {
        void write(Registers registers, int value);
    }

    /**
     * An interface that is mostly used to be able to use a single register as method reference.
     * This is for instructions that does the same thing to different registers for different opcodes.
     */
    @FunctionalInterface
    interface RegisterReader {
        int read(Registers registers);
    }

    /**
     * A full list of all the instructions that the CPU can execute with a mapping between the opcode
     * and a method reference that serves as a factory for the instruction.
     *
     * STOP and HALT needs to be overridden as they don't operate on the provided parameters
     *
     * See this file for reference: http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
     * (where the instructions can be found is commented on each enum value)
     */
    enum InstructionType implements EnumByValue.ComparableByInt {
        NOP(        0x00, NoOp::new), //Page 97
        LD_BC_nn(   0x01, LoadWordIntoRegister::toBC), //Page 76
        LD_BC_A(    0x02, LoadRegisterIntoAddressOfWordRegister::AtoBC), //Page 69
        INC_BC(     0x03, IncrementWordRegister::BC), //Page 92
        INC_B(      0x04, IncrementByteRegister::B), //Page 88
        DEC_B(      0x05, DecrementByteRegister::B), //Page 89
        LD_B_n(     0x06, LoadByteIntoRegister::toB), //Page 65
        RLCA(       0x07, RotateRegisterLeft::AresetZero), //Page 99
        LD_nn_SP(   0x08, LoadStackPointerToAddressOfWord::new), //Page 78
        ADD_HL_BC(  0x09, AddWordRegisterIntoRegister::BCtoHL), //Page 90
        LD_A_BC(    0x0A, LoadAddressOfRegisterIntoRegister::fromBCtoA), //Page 68
        DEC_BC(     0x0B, DecrementWordRegister::BC), //Page 93
        INC_C(      0x0C, IncrementByteRegister::C), //Page 88
        DEC_C(      0x0D, DecrementByteRegister::C), //Page 89
        LD_C_n(     0x0E, LoadByteIntoRegister::toC), //Page 65
        RRCA(       0x0F, RotateRegisterRight::AresetZero), //Page 100

        STOP(       0x10, InvalidInstruction::new), //Page 97
        LD_DE_nn(   0x11, LoadWordIntoRegister::toDE), //Page 76
        LD_DE_A(    0x12, LoadRegisterIntoAddressOfWordRegister::AtoDE), //Page 69
        INC_DE(     0x13, IncrementWordRegister::DE), //Page 92
        INC_D(      0x14, IncrementByteRegister::D), //Page 88
        DEC_D(      0x15, DecrementByteRegister::D), //Page 89
        LD_D_n(     0x16, LoadByteIntoRegister::toD), //Page 65
        RLA(        0x17, RotateRegisterLeft::AthroughCarryResetZero), //Page 99
        JR_n(       0x18, JumpRelative::unconditional), //Page 112
        ADD_HL_DE(  0x19, AddWordRegisterIntoRegister::DEtoHL), //Page 90
        LD_A_DE(    0x1A, LoadAddressOfRegisterIntoRegister::fromDEtoA), //Page 68
        DEC_DE(     0x1B, DecrementWordRegister::DE), //Page 93
        INC_E(      0x1C, IncrementByteRegister::E), //Page 88
        DEC_E(      0x1D, DecrementByteRegister::E), //Page 89
        LD_E_n(     0x1E, LoadByteIntoRegister::toE), //Page 65
        RRA(        0x1F, RotateRegisterRight::AthroughCarryResetZero), //Page 100

        JR_NZ_n(    0x20, JumpRelative::ifLastNotZero), //Page 113
        LD_HL_nn(   0x21, LoadWordIntoRegister::toHL), //Page 76
        LDI_HL_A(   0x22, LoadAIntoAddressOfHLIncreased::new), //Page 74
        INC_HL(     0x23, IncrementWordRegister::HL), //Page 92,
        INC_H(      0x24, IncrementByteRegister::H), //Page 88
        DEC_H(      0x25, DecrementByteRegister::H), //Page 89
        LD_H_n(     0x26, LoadByteIntoRegister::toH), //Page 65
        DAA(        0x27, DecimalAdjustA::new), //Page 95
        JR_Z_n(     0x28, JumpRelative::ifLastZero), //Page 113
        ADD_HL_HL(  0x29, AddWordRegisterIntoRegister::HLtoHL), //Page 90
        LDI_A_HL(   0x2A, LoadAddressOfHLIncreasedIntoA::new), //Page 73
        DEC_HL(     0x2B, DecrementWordRegister::HL), //Page 93
        INC_L(      0x2C, IncrementByteRegister::L), //Page 88
        DEC_L(      0x2D, DecrementByteRegister::L), //Page 89
        LD_L_n(     0x2E, LoadByteIntoRegister::toL), //Page 65
        CPL(        0x2F, ComplementA::new), //Page 95

        JR_NC_n(    0x30, JumpRelative::ifLastNotCarry), //Page 113
        LD_SP_nn(   0x31, LoadWordIntoStackPointer::new), //Page 76
        LDD_HL_A(   0x32, LoadAIntoAddressOfHLDecreased::new), //Page 72
        INC_SP(     0x33, IncrementStackPointer::new), //Page 92
        INC_HL_n(   0x34, IncrementByteAddressOfHL::new), //Page 88
        DEC_HL_n(   0x35, DecrementAddressOfHL::new), //Page 89
        LD_HL_n(    0x36, LoadByteIntoAddressOfHL::new), //Page 67
        SCF(        0x37, SetCarryFlag::new), //Page 96
        JR_C(       0x38, JumpRelative::ifLastCarry), //Page 113
        ADD_HL_SP(  0x39, AddStackPointerIntoHL::new), //Page 90
        LDD_A_HL(   0x3A, LoadAddressOfHLDecreasedIntoA::new), //Page 71
        DEC_SP(     0x3B, DecrementStackPointer::new), //Page 93
        INC_A(      0x3C, IncrementByteRegister::A), //Page 88
        DEC_A(      0x3D, DecrementByteRegister::A), //Page 89
        LD_A_n(     0x3E, LoadByteIntoRegister::toA), //Page 68
        CCF(        0x3F, ComplementCarryFlag::new), //Page 96

        LD_B_B(     0x40, LoadRegisterIntoRegister::fromBtoB), //Page 66
        LD_B_C(     0x41, LoadRegisterIntoRegister::fromCtoB), //Page 66
        LD_B_D(     0x42, LoadRegisterIntoRegister::fromDtoB), //Page 66
        LD_B_E(     0x43, LoadRegisterIntoRegister::fromEtoB), //Page 66
        LD_B_H(     0x44, LoadRegisterIntoRegister::fromHtoB), //Page 66
        LD_B_L(     0x45, LoadRegisterIntoRegister::fromLtoB), //Page 66
        LD_B_HL(    0x46, LoadAddressOfRegisterIntoRegister::fromHLtoB), //Page 66
        LD_B_A(     0x47, LoadRegisterIntoRegister::fromAtoB), //Page 69
        LD_C_B(     0x48, LoadRegisterIntoRegister::fromBtoC), //Page 66
        LD_C_C(     0x49, LoadRegisterIntoRegister::fromCtoC), //Page 66
        LD_C_D(     0x4A, LoadRegisterIntoRegister::fromDtoC), //Page 66
        LD_C_E(     0x4B, LoadRegisterIntoRegister::fromEtoC), //Page 66
        LD_C_H(     0x4C, LoadRegisterIntoRegister::fromHtoC), //Page 66
        LD_C_L(     0x4D, LoadRegisterIntoRegister::fromLtoC), //Page 66
        LD_C_HL(    0x4E, LoadAddressOfRegisterIntoRegister::fromHLtoC), //Page 66
        LD_C_A(     0x4F, LoadRegisterIntoRegister::fromAtoC), //Page 69

        LD_D_B(     0x50, LoadRegisterIntoRegister::fromBtoD), //Page 66
        LD_D_C(     0x51, LoadRegisterIntoRegister::fromCtoD), //Page 66
        LD_D_D(     0x52, LoadRegisterIntoRegister::fromDtoD), //Page 66
        LD_D_E(     0x53, LoadRegisterIntoRegister::fromEtoD), //Page 66
        LD_D_H(     0x54, LoadRegisterIntoRegister::fromHtoD), //Page 66
        LD_D_L(     0x55, LoadRegisterIntoRegister::fromLtoD), //Page 66
        LD_D_HL(    0x56, LoadAddressOfRegisterIntoRegister::fromHLtoD), //Page 66
        LD_D_A(     0x57, LoadRegisterIntoRegister::fromAtoD), //Page 69
        LD_E_B(     0x58, LoadRegisterIntoRegister::fromBtoE), //Page 66
        LD_E_C(     0x59, LoadRegisterIntoRegister::fromCtoE), //Page 66
        LD_E_D(     0x5A, LoadRegisterIntoRegister::fromDtoE), //Page 66
        LD_E_E(     0x5B, LoadRegisterIntoRegister::fromEtoE), //Page 66
        LD_E_H(     0x5C, LoadRegisterIntoRegister::fromHtoE), //Page 66
        LD_E_L(     0x5D, LoadRegisterIntoRegister::fromLtoE), //Page 66
        LD_E_HL(    0x5E, LoadAddressOfRegisterIntoRegister::fromHLtoE), //Page 66
        LD_E_A(     0x5F, LoadRegisterIntoRegister::fromAtoE), //Page 69

        LD_H_B(     0x60, LoadRegisterIntoRegister::fromBtoH), //Page 66
        LD_H_C(     0x61, LoadRegisterIntoRegister::fromCtoH), //Page 66
        LD_H_D(     0x62, LoadRegisterIntoRegister::fromDtoH), //Page 66
        LD_H_E(     0x63, LoadRegisterIntoRegister::fromEtoH), //Page 66
        LD_H_H(     0x64, LoadRegisterIntoRegister::fromHtoH), //Page 66
        LD_H_L(     0x65, LoadRegisterIntoRegister::fromLtoH), //Page 66
        LD_H_HL(    0x66, LoadAddressOfRegisterIntoRegister::fromHLtoH), //Page 66
        LD_H_A(     0x67, LoadRegisterIntoRegister::fromAtoH), //Page 69
        LD_L_B(     0x68, LoadRegisterIntoRegister::fromBtoL), //Page 66
        LD_L_C(     0x69, LoadRegisterIntoRegister::fromCtoL), //Page 66
        LD_L_D(     0x6A, LoadRegisterIntoRegister::fromDtoL), //Page 66
        LD_L_E(     0x6B, LoadRegisterIntoRegister::fromEtoL), //Page 66
        LD_L_H(     0x6C, LoadRegisterIntoRegister::fromHtoL), //Page 66
        LD_L_L(     0x6D, LoadRegisterIntoRegister::fromLtoL), //Page 66
        LD_L_HL(    0x6E, LoadAddressOfRegisterIntoRegister::fromHLtoL), //Page 66
        LD_L_A(     0x6F, LoadRegisterIntoRegister::fromAtoL), //Page 69

        LD_HL_B(    0x70, LoadRegisterIntoAddressOfWordRegister::BtoHL), //Page 67
        LD_HL_C(    0x71, LoadRegisterIntoAddressOfWordRegister::CtoHL), //Page 67
        LD_HL_D(    0x72, LoadRegisterIntoAddressOfWordRegister::DtoHL), //Page 67
        LD_HL_E(    0x73, LoadRegisterIntoAddressOfWordRegister::EtoHL), //Page 67
        LD_HL_H(    0x74, LoadRegisterIntoAddressOfWordRegister::HtoHL), //Page 67
        LD_HL_L(    0x75, LoadRegisterIntoAddressOfWordRegister::LtoHL), //Page 67
        HALT(       0x76, InvalidInstruction::new), //Page 97
        LD_HL_A(    0x77, LoadRegisterIntoAddressOfWordRegister::AtoHL), //Page 69
        LD_A_B(     0x78, LoadRegisterIntoRegister::fromBtoA), //Page 66
        LD_A_C(     0x79, LoadRegisterIntoRegister::fromCtoA), //Page 66
        LD_A_D(     0x7A, LoadRegisterIntoRegister::fromDtoA), //Page 66
        LD_A_E(     0x7B, LoadRegisterIntoRegister::fromEtoA), //Page 66
        LD_A_H(     0x7C, LoadRegisterIntoRegister::fromHtoA), //Page 66
        LD_A_L(     0x7D, LoadRegisterIntoRegister::fromLtoA), //Page 66
        LD_A_HL(    0x7E, LoadAddressOfRegisterIntoRegister::fromHLtoA), //Page 66
        LD_A_A(     0x7F, LoadRegisterIntoRegister::fromAtoA), //Page 68

        ADD_A_B(    0x80, AddRegisterIntoA::B), //Page 80
        ADD_A_C(    0x81, AddRegisterIntoA::C), //Page 80
        ADD_A_D(    0x82, AddRegisterIntoA::D), //Page 80
        ADD_A_E(    0x83, AddRegisterIntoA::E), //Page 80
        ADD_A_H(    0x84, AddRegisterIntoA::H), //Page 80
        ADD_A_L(    0x85, AddRegisterIntoA::L), //Page 80
        ADD_A_HL(   0x86, AddAddressOfHLIntoA::new), //Page 80
        ADD_A_A(    0x87, AddRegisterIntoA::A), //Page 80
        ADC_A_B(    0x88, AddRegisterWithCarryIntoA::B), //Page 81
        ADC_A_C(    0x89, AddRegisterWithCarryIntoA::C), //Page 81
        ADC_A_D(    0x8A, AddRegisterWithCarryIntoA::D), //Page 81
        ADC_A_E(    0x8B, AddRegisterWithCarryIntoA::E), //Page 81
        ADC_A_H(    0x8C, AddRegisterWithCarryIntoA::H), //Page 81
        ADC_A_L(    0x8D, AddRegisterWithCarryIntoA::L), //Page 81
        ADC_A_HL(   0x8E, AddAddressOfHLWithCarryIntoA::new), //Page 81
        ADC_A_A(    0x8F, AddRegisterWithCarryIntoA::A), //Page 81

        SUB_B(      0x90, SubtractRegisterFromA::fromB), //Page 82
        SUB_C(      0x91, SubtractRegisterFromA::fromC), //Page 82
        SUB_D(      0x92, SubtractRegisterFromA::fromD), //Page 82
        SUB_E(      0x93, SubtractRegisterFromA::fromE), //Page 82
        SUB_H(      0x94, SubtractRegisterFromA::fromH), //Page 82
        SUB_L(      0x95, SubtractRegisterFromA::fromL), //Page 82
        SUB_HL(     0x96, SubtractAddressOfHLFromA::withoutCarry), //page 96
        SUB_A(      0x97, SubtractRegisterFromA::fromA), //Page 82
        SBC_B(      0x98, SubtractRegisterWithCarryFromA::fromB), //Page 83
        SBC_C(      0x99, SubtractRegisterWithCarryFromA::fromC), //Page 83
        SBC_D(      0x9A, SubtractRegisterWithCarryFromA::fromD), //Page 83
        SBC_E(      0x9B, SubtractRegisterWithCarryFromA::fromE), //Page 83
        SBC_H(      0x9C, SubtractRegisterWithCarryFromA::fromH), //Page 83
        SBC_L(      0x9D, SubtractRegisterWithCarryFromA::fromL), //Page 83
        SBC_HL(     0x9E, SubtractAddressOfHLFromA::withCarry), //Page 83
        SBC_A(      0x9F, SubtractRegisterWithCarryFromA::fromA), //Page 83

        AND_B(      0xA0, AndRegisterWithA::B), //Page 84
        AND_C(      0xA1, AndRegisterWithA::C), //Page 84
        AND_D(      0xA2, AndRegisterWithA::D), //Page 84
        AND_E(      0xA3, AndRegisterWithA::E), //Page 84
        AND_H(      0xA4, AndRegisterWithA::H), //Page 84
        AND_L(      0xA5, AndRegisterWithA::L), //Page 84
        AND_HL(     0xA6, AndAddressOfHLWithA::new), //Page 84
        AND_A(      0xA7, AndRegisterWithA::A), //Page 84
        XOR_B(      0xA8, XorRegisterWithA::B), //Page 86
        XOR_C(      0xA9, XorRegisterWithA::C), //Page 86
        XOR_D(      0xAA, XorRegisterWithA::D), //Page 86
        XOR_E(      0xAB, XorRegisterWithA::E), //Page 86
        XOR_H(      0xAC, XorRegisterWithA::H), //Page 86
        XOR_L(      0xAD, XorRegisterWithA::L), //Page 86
        XOR_HL(     0xAE, XorAddressOfHLWithA::new), //Page 86
        XOR_A(      0xAF, XorRegisterWithA::A), //Page 86

        OR_B(       0xB0, OrRegisterWithA::B), //Page 85
        OR_C(       0xB1, OrRegisterWithA::C), //Page 85
        OR_D(       0xB2, OrRegisterWithA::D), //Page 85
        OR_E(       0xB3, OrRegisterWithA::E), //Page 85
        OR_H(       0xB4, OrRegisterWithA::H), //Page 85
        OR_L(       0xB5, OrRegisterWithA::L), //Page 85
        OR_HL(      0xB6, OrAddressOfHLWithA::new), //Page 85
        OR_A(       0xB7, OrRegisterWithA::A), //Page 85
        CP_B(       0xB8, CompareRegisterAgainstA::B), //Page 85
        CP_C(       0xB9, CompareRegisterAgainstA::C), //Page 85
        CP_D(       0xBA, CompareRegisterAgainstA::D), //Page 85
        CP_E(       0xBB, CompareRegisterAgainstA::E), //Page 85
        CP_H(       0xBC, CompareRegisterAgainstA::H), //Page 85
        CP_L(       0xBD, CompareRegisterAgainstA::L), //Page 85
        CP_HL(      0xBE, CompareByteAddressOfHLAgainstA::new), //Page 87
        CP_A(       0xBF, CompareRegisterAgainstA::A), //Page 85

        RET_NZ(     0xC0, Return::ifNotZero), //Page 117
        POP_BC(     0xC1, PopStackIntoRegister::toBC), //Page 79
        JP_NZ(      0xC2, JumpWord::ifLastNotZero), //Page 111
        JP_nn(      0xC3, JumpWord::unconditional), //Page 111
        CALL_NZ(    0xC4, CallRoutine::ifLastNotZero), //Page 115
        PUSH_BC(    0xC5, PushRegisterIntoStack::fromBC), //Page 78
        ADD_n(      0xC6, AddByteIntoA::new), //Page 80
        RST_00(     0xC7, Restart::to00), //Page 116
        RET_Z(      0xC8, Return::ifZero), //Page 117
        RET(        0xC9, Return::unconditional), //Page 117
        JP_Z(       0xCA, JumpWord::ifLastZero), //Page 111
        CB(         0xCB, InvalidInstruction::new), //Page 99-110, special case (append 0xCB before CB-instructions)
        CALL_Z(     0xCC, CallRoutine::ifLastZero), //Page 114
        CALL_nn(    0xCD, CallRoutine::unconditional), //Page 114
        ADC_A_n(    0xCE, AddByteWithCarryIntoA::new), //Page 81
        RST_08(     0xCF, Restart::to08), //Page 116

        RET_NC(     0xD0, Return::ifNotCarry), //Page 117
        POP_DE(     0xD1, PopStackIntoRegister::toDE), //Page 79
        JP_NC(      0xD2, JumpWord::ifLastNotCarry), //Page 111
        CALL_NC(    0xD4, CallRoutine::ifLastNotCarry), //Page 114
        PUSH_DE(    0xD5, PushRegisterIntoStack::fromDE), //Page 78
        SUB_n(      0xD6, SubtractByteFromA::withoutCarry), //Page 82
        RST_10(     0xD7, Restart::to10), //Page 116
        RET_C(      0xD8, Return::ifCarry), //Page 117
        RETI(       0xD9, Return::andEnableInterrupts), //Page 118
        JP_C(       0xDA, JumpWord::ifLastCarry), //Page 111
        CALL_C(     0xDC, CallRoutine::ifLastCarry), //Page 114
        SBC_n(      0xDE, SubtractByteFromA::withCarry), //Page 83
        RST_18(     0xDF, Restart::to18), //Page 116

        LDH_n_A(    0xE0, LoadAIntoAddressOffsetByte::new), //Page 75
        POP_HL(     0xE1, PopStackIntoRegister::toHL), //Page 79
        LDH_C_A(    0xE2, LoadAIntoAddressOffsetC::new), //Page 70
        PUSH_HL(    0xE5, PushRegisterIntoStack::fromHL), //Page 78
        AND_n(      0xE6, AndByteWithA::new), //Page 84
        RST_20(     0xE7, Restart::to20), //Page 116
        ADD_SP_n(   0xE8, AddByteToStackPointer::new), //Page 91
        JP_HL(      0xE9, JumpToValueOfHL::new), //Page 112
        LD_nn_A(    0xEA, LoadAIntoAddressOfWord::new), //Page 69
        XOR_n(      0xEE, XorByteWithA::new), //Page 86
        RST_28(     0xEF, Restart::to28), //Page 116

        LDH_A_n(    0xF0, LoadAddressOfByteOffsetIntoA::new), //Page 75
        POP_AF(     0xF1, PopStackIntoRegister::toAF), //Page 79
        LDH_A_C(    0xF2, LoadAddressOfOffsetCIntoA::new), //Page 70
        DI(         0xF3, DisableInterrupts::new), //Page 98
        PUSH_AF(    0xF5, PushRegisterIntoStack::fromAF), //Page 78
        OR_n(       0xF6, OrByteWithA::new), //Page 85
        RST_30(     0xF7, Restart::to30), //Page 116
        LD_HL_SP_n( 0xF8, LoadStackPointerOffsetByteIntoHL::new), //Page 77
        LD_SP_HL(   0xF9, LoadHLIntoStackPointer::new), //Page 76
        LD_A_nn(    0xFA, LoadAddressOfWordIntoA::new), //Page 68
        EI(         0xFB, EnableInterrupts::new), //Page 98
        CP_n(       0xFE, CompareByteAgainstA::new), //Page 87
        RST_38(     0xFF, Restart::to38), //Page 116

        RLC_B(      0xCB00, RotateRegisterLeft::B), //Page 101
        RLC_C(      0xCB01, RotateRegisterLeft::C), //Page 101
        RLC_D(      0xCB02, RotateRegisterLeft::D), //Page 101
        RLC_E(      0xCB03, RotateRegisterLeft::E), //Page 101
        RLC_H(      0xCB04, RotateRegisterLeft::H), //Page 101
        RLC_L(      0xCB05, RotateRegisterLeft::L), //Page 101
        RLC_HL(     0xCB06, RotateAddressOfHLLeft::bit7), //Page 101
        RLC_A(      0xCB07, RotateRegisterLeft::A), //Page 101
        RRC_B(      0xCB08, RotateRegisterRight::B), //Page 103
        RRC_C(      0xCB09, RotateRegisterRight::C), //Page 103
        RRC_D(      0xCB0A, RotateRegisterRight::D), //Page 103
        RRC_E(      0xCB0B, RotateRegisterRight::E), //Page 103
        RRC_H(      0xCB0C, RotateRegisterRight::H), //Page 103
        RRC_L(      0xCB0D, RotateRegisterRight::L), //Page 103
        RRC_HL(     0xCB0E, RotateAddressOfHLRight::bit0), //Page 103
        RRC_A(      0xCB0F, RotateRegisterRight::A), //Page 103

        RL_B(       0xCB10, RotateRegisterLeft::BthroughCarry), //Page 102
        RL_C(       0xCB11, RotateRegisterLeft::CthroughCarry), //Page 102
        RL_D(       0xCB12, RotateRegisterLeft::DthroughCarry), //Page 102
        RL_E(       0xCB13, RotateRegisterLeft::EthroughCarry), //Page 102
        RL_H(       0xCB14, RotateRegisterLeft::HthroughCarry), //Page 102
        RL_L(       0xCB15, RotateRegisterLeft::LthroughCarry), //Page 102
        RL_HL(      0xCB16, RotateAddressOfHLLeft::flag), //Page 102
        RL_A(       0xCB17, RotateRegisterLeft::AthroughCarry), //Page 102
        RR_B(       0xCB18, RotateRegisterRight::BthroughCarry), //Page 104
        RR_C(       0xCB19, RotateRegisterRight::CthroughCarry), //Page 104
        RR_D(       0xCB1A, RotateRegisterRight::DthroughCarry), //Page 104
        RR_E(       0xCB1B, RotateRegisterRight::EthroughCarry), //Page 104
        RR_H(       0xCB1C, RotateRegisterRight::HthroughCarry), //Page 104
        RR_L(       0xCB1D, RotateRegisterRight::LthroughCarry), //Page 104
        RR_HL(      0xCB1E, RotateAddressOfHLRight::flag), //Page 104
        RR_A(       0xCB1F, RotateRegisterRight::AthroughCarry), //Page 104

        SLA_B(      0xCB20, ShiftRegisterLeft::B), //Page 105
        SLA_C(      0xCB21, ShiftRegisterLeft::C), //Page 105
        SLA_D(      0xCB22, ShiftRegisterLeft::D), //Page 105
        SLA_E(      0xCB23, ShiftRegisterLeft::E), //Page 105
        SLA_H(      0xCB24, ShiftRegisterLeft::H), //Page 105
        SLA_L(      0xCB25, ShiftRegisterLeft::L), //Page 105
        SLA_HL(     0xCB26, ShiftAddressOfHLLeft::new), //Page 105
        SLA_A(      0xCB27, ShiftRegisterLeft::A), //Page 105
        SRA_B(      0xCB28, ShiftRegisterRight::BkeepBit7), //Page 106
        SRA_C(      0xCB29, ShiftRegisterRight::CkeepBit7), //Page 106
        SRA_D(      0xCB2A, ShiftRegisterRight::DkeepBit7), //Page 106
        SRA_E(      0xCB2B, ShiftRegisterRight::EkeepBit7), //Page 106
        SRA_H(      0xCB2C, ShiftRegisterRight::HkeepBit7), //Page 106
        SRA_L(      0xCB2D, ShiftRegisterRight::LkeepBit7), //Page 106
        SRA_HL(     0xCB2E, ShiftAddressOfHLRight::keepBit7), //Page 106
        SRA_A(      0xCB2F, ShiftRegisterRight::AkeepBit7), //Page 106

        SWAP_B(     0xCB30, SwapRegister::B), //Page 94
        SWAP_C(     0xCB31, SwapRegister::C), //Page 94
        SWAP_D(     0xCB32, SwapRegister::D), //Page 94
        SWAP_E(     0xCB33, SwapRegister::E), //Page 94
        SWAP_H(     0xCB34, SwapRegister::H), //Page 94
        SWAP_L(     0xCB35, SwapRegister::L), //Page 94
        SWAP_HL(    0xCB36, SwapAddressOfHL::new), //Page 94
        SWAP_A(     0xCB37, SwapRegister::A), //Page 94
        SRL_B(      0xCB38, ShiftRegisterRight::B), //Page 107
        SRL_C(      0xCB39, ShiftRegisterRight::C), //Page 107
        SRL_D(      0xCB3A, ShiftRegisterRight::D), //Page 107
        SRL_E(      0xCB3B, ShiftRegisterRight::E), //Page 107
        SRL_H(      0xCB3C, ShiftRegisterRight::H), //Page 107
        SRL_L(      0xCB3D, ShiftRegisterRight::L), //Page 107
        SRL_HL(     0xCB3E, ShiftAddressOfHLRight::resetBit7), //Page 107
        SRL_A(      0xCB3F, ShiftRegisterRight::A), //Page 107

        BIT_0B(     0xCB40, CompareBit::bit0ofB), //Page 108
        BIT_0C(     0xCB41, CompareBit::bit0ofC), //Page 108
        BIT_0D(     0xCB42, CompareBit::bit0ofD), //Page 108
        BIT_0E(     0xCB43, CompareBit::bit0ofE), //Page 108
        BIT_0H(     0xCB44, CompareBit::bit0ofH), //Page 108
        BIT_0L(     0xCB45, CompareBit::bit0ofL), //Page 108
        BIT_0_HL(   0xCB46, CompareBitAddressOfHL::bit0), //Page 108
        BIT_0A(     0xCB47, CompareBit::bit0ofA), //Page 108
        BIT_1B(     0xCB48, CompareBit::bit1ofB), //Page 108
        BIT_1C(     0xCB49, CompareBit::bit1ofC), //Page 108
        BIT_1D(     0xCB4A, CompareBit::bit1ofD), //Page 108
        BIT_1E(     0xCB4B, CompareBit::bit1ofE), //Page 108
        BIT_1H(     0xCB4C, CompareBit::bit1ofH), //Page 108
        BIT_1L(     0xCB4D, CompareBit::bit1ofL), //Page 108
        BIT_1_HL(   0xCB4E, CompareBitAddressOfHL::bit1), //Page 108
        BIT_1A(     0xCB4F, CompareBit::bit1ofA), //Page 108

        BIT_2B(     0xCB50, CompareBit::bit2ofB), //Page 108
        BIT_2C(     0xCB51, CompareBit::bit2ofC), //Page 108
        BIT_2D(     0xCB52, CompareBit::bit2ofD), //Page 108
        BIT_2E(     0xCB53, CompareBit::bit2ofE), //Page 108
        BIT_2H(     0xCB54, CompareBit::bit2ofH), //Page 108
        BIT_2L(     0xCB55, CompareBit::bit2ofL), //Page 108
        BIT_2_HL(   0xCB56, CompareBitAddressOfHL::bit2), //Page 108
        BIT_2A(     0xCB57, CompareBit::bit2ofA), //Page 108
        BIT_3B(     0xCB58, CompareBit::bit3ofB), //Page 108
        BIT_3C(     0xCB59, CompareBit::bit3ofC), //Page 108
        BIT_3D(     0xCB5A, CompareBit::bit3ofD), //Page 108
        BIT_3E(     0xCB5B, CompareBit::bit3ofE), //Page 108
        BIT_3H(     0xCB5C, CompareBit::bit3ofH), //Page 108
        BIT_3L(     0xCB5D, CompareBit::bit3ofL), //Page 108
        BIT_3_HL(   0xCB5E, CompareBitAddressOfHL::bit3), //Page 108
        BIT_3A(     0xCB5F, CompareBit::bit3ofA), //Page 108

        BIT_4B(     0xCB60, CompareBit::bit4ofB), //Page 108
        BIT_4C(     0xCB61, CompareBit::bit4ofC), //Page 108
        BIT_4D(     0xCB62, CompareBit::bit4ofD), //Page 108
        BIT_4E(     0xCB63, CompareBit::bit4ofE), //Page 108
        BIT_4H(     0xCB64, CompareBit::bit4ofH), //Page 108
        BIT_4L(     0xCB65, CompareBit::bit4ofL), //Page 108
        BIT_4_HL(   0xCB66, CompareBitAddressOfHL::bit4), //Page 108
        BIT_4A(     0xCB67, CompareBit::bit4ofA), //Page 108
        BIT_5B(     0xCB68, CompareBit::bit5ofB), //Page 108
        BIT_5C(     0xCB69, CompareBit::bit5ofC), //Page 108
        BIT_5D(     0xCB6A, CompareBit::bit5ofD), //Page 108
        BIT_5E(     0xCB6B, CompareBit::bit5ofE), //Page 108
        BIT_5H(     0xCB6C, CompareBit::bit5ofH), //Page 108
        BIT_5L(     0xCB6D, CompareBit::bit5ofL), //Page 108
        BIT_5_HL(   0xCB6E, CompareBitAddressOfHL::bit5), //Page 108
        BIT_5A(     0xCB6F, CompareBit::bit5ofA), //Page 108

        BIT_6B(     0xCB70, CompareBit::bit6ofB), //Page 108
        BIT_6C(     0xCB71, CompareBit::bit6ofC), //Page 108
        BIT_6D(     0xCB72, CompareBit::bit6ofD), //Page 108
        BIT_6E(     0xCB73, CompareBit::bit6ofE), //Page 108
        BIT_6H(     0xCB74, CompareBit::bit6ofH), //Page 108
        BIT_6L(     0xCB75, CompareBit::bit6ofL), //Page 108
        BIT_6_HL(   0xCB76, CompareBitAddressOfHL::bit6), //Page 108
        BIT_6A(     0xCB77, CompareBit::bit6ofA), //Page 108
        BIT_7B(     0xCB78, CompareBit::bit7ofB), //Page 108
        BIT_7C(     0xCB79, CompareBit::bit7ofC), //Page 108
        BIT_7D(     0xCB7A, CompareBit::bit7ofD), //Page 108
        BIT_7E(     0xCB7B, CompareBit::bit7ofE), //Page 108
        BIT_7H(     0xCB7C, CompareBit::bit7ofH), //Page 108
        BIT_7L(     0xCB7D, CompareBit::bit7ofL), //Page 108
        BIT_7_HL(   0xCB7E, CompareBitAddressOfHL::bit7), //Page 108
        BIT_7A(     0xCB7F, CompareBit::bit7ofA), //Page 108

        RES_B0(     0xCB80, ResetBitInRegister::bit0ofB), //Page 110
        RES_C0(     0xCB81, ResetBitInRegister::bit0ofC), //Page 110
        RES_D0(     0xCB82, ResetBitInRegister::bit0ofD), //Page 110
        RES_E0(     0xCB83, ResetBitInRegister::bit0ofE), //Page 110
        RES_H0(     0xCB84, ResetBitInRegister::bit0ofH), //Page 110
        RES_L0(     0xCB85, ResetBitInRegister::bit0ofL), //Page 110
        RES_0_HL(   0xCB86, ResetBitAddressOfHL::bit0), //Page 110
        RES_A0(     0xCB87, ResetBitInRegister::bit0ofA), //Page 110
        RES_B1(     0xCB88, ResetBitInRegister::bit1ofB), //Page 110
        RES_C1(     0xCB89, ResetBitInRegister::bit1ofC), //Page 110
        RES_D1(     0xCB8A, ResetBitInRegister::bit1ofD), //Page 110
        RES_E1(     0xCB8B, ResetBitInRegister::bit1ofE), //Page 110
        RES_H1(     0xCB8C, ResetBitInRegister::bit1ofH), //Page 110
        RES_L1(     0xCB8D, ResetBitInRegister::bit1ofL), //Page 110
        RES_1_HL(   0xCB8E, ResetBitAddressOfHL::bit1), //Page 110
        RES_A1(     0xCB8F, ResetBitInRegister::bit1ofA), //Page 110

        RES_B2(     0xCB90, ResetBitInRegister::bit2ofB), //Page 110
        RES_C2(     0xCB91, ResetBitInRegister::bit2ofC), //Page 110
        RES_D2(     0xCB92, ResetBitInRegister::bit2ofD), //Page 110
        RES_E2(     0xCB93, ResetBitInRegister::bit2ofE), //Page 110
        RES_H2(     0xCB94, ResetBitInRegister::bit2ofH), //Page 110
        RES_L2(     0xCB95, ResetBitInRegister::bit2ofL), //Page 110
        RES_2_HL(   0xCB96, ResetBitAddressOfHL::bit2), //Page 110
        RES_A2(     0xCB97, ResetBitInRegister::bit2ofA), //Page 110
        RES_B3(     0xCB98, ResetBitInRegister::bit3ofB), //Page 110
        RES_C3(     0xCB99, ResetBitInRegister::bit3ofC), //Page 110
        RES_D3(     0xCB9A, ResetBitInRegister::bit3ofD), //Page 110
        RES_E3(     0xCB9B, ResetBitInRegister::bit3ofE), //Page 110
        RES_H3(     0xCB9C, ResetBitInRegister::bit3ofH), //Page 110
        RES_L3(     0xCB9D, ResetBitInRegister::bit3ofL), //Page 110
        RES_3_HL(   0xCB9E, ResetBitAddressOfHL::bit3), //Page 110
        RES_A3(     0xCB9F, ResetBitInRegister::bit3ofA), //Page 110

        RES_B4(     0xCBA0, ResetBitInRegister::bit4ofB), //Page 110
        RES_C4(     0xCBA1, ResetBitInRegister::bit4ofC), //Page 110
        RES_D4(     0xCBA2, ResetBitInRegister::bit4ofD), //Page 110
        RES_E4(     0xCBA3, ResetBitInRegister::bit4ofE), //Page 110
        RES_H4(     0xCBA4, ResetBitInRegister::bit4ofH), //Page 110
        RES_L4(     0xCBA5, ResetBitInRegister::bit4ofL), //Page 110
        RES_4_HL(   0xCBA6, ResetBitAddressOfHL::bit4), //Page 110
        RES_A4(     0xCBA7, ResetBitInRegister::bit4ofA), //Page 110
        RES_B5(     0xCBA8, ResetBitInRegister::bit5ofB), //Page 110
        RES_C5(     0xCBA9, ResetBitInRegister::bit5ofC), //Page 110
        RES_D5(     0xCBAA, ResetBitInRegister::bit5ofD), //Page 110
        RES_E5(     0xCBAB, ResetBitInRegister::bit5ofE), //Page 110
        RES_H5(     0xCBAC, ResetBitInRegister::bit5ofH), //Page 110
        RES_L5(     0xCBAD, ResetBitInRegister::bit5ofL), //Page 110
        RES_5_HL(   0xCBAE, ResetBitAddressOfHL::bit5), //Page 110
        RES_A5(     0xCBAF, ResetBitInRegister::bit5ofA), //Page 110

        RES_B6(     0xCBB0, ResetBitInRegister::bit6ofB), //Page 110
        RES_C6(     0xCBB1, ResetBitInRegister::bit6ofC), //Page 110
        RES_D6(     0xCBB2, ResetBitInRegister::bit6ofD), //Page 110
        RES_E6(     0xCBB3, ResetBitInRegister::bit6ofE), //Page 110
        RES_H6(     0xCBB4, ResetBitInRegister::bit6ofH), //Page 110
        RES_L6(     0xCBB5, ResetBitInRegister::bit6ofL), //Page 110
        RES_6_HL(   0xCBB6, ResetBitAddressOfHL::bit6), //Page 110
        RES_A6(     0xCBB7, ResetBitInRegister::bit6ofA), //Page 110
        RES_B7(     0xCBB8, ResetBitInRegister::bit7ofB), //Page 110
        RES_C7(     0xCBB9, ResetBitInRegister::bit7ofC), //Page 110
        RES_D7(     0xCBBA, ResetBitInRegister::bit7ofD), //Page 110
        RES_E7(     0xCBBB, ResetBitInRegister::bit7ofE), //Page 110
        RES_H7(     0xCBBC, ResetBitInRegister::bit7ofH), //Page 110
        RES_L7(     0xCBBD, ResetBitInRegister::bit7ofL), //Page 110
        RES_7_HL(   0xCBBE, ResetBitAddressOfHL::bit7), //Page 110
        RES_A7(     0xCBBF, ResetBitInRegister::bit7ofA), //Page 110

        SET_B0(     0xCBC0, SetBitInRegister::bit0ofB), //Page 109
        SET_C0(     0xCBC1, SetBitInRegister::bit0ofC), //Page 109
        SET_D0(     0xCBC2, SetBitInRegister::bit0ofD), //Page 109
        SET_E0(     0xCBC3, SetBitInRegister::bit0ofE), //Page 109
        SET_H0(     0xCBC4, SetBitInRegister::bit0ofH), //Page 109
        SET_L0(     0xCBC5, SetBitInRegister::bit0ofL), //Page 109
        SET_0_HL(   0xCBC6, SetBitAddressOfHL::bit0), //Page 109
        SET_A0(     0xCBC7, SetBitInRegister::bit0ofA), //Page 109
        SET_B1(     0xCBC8, SetBitInRegister::bit1ofB), //Page 109
        SET_C1(     0xCBC9, SetBitInRegister::bit1ofC), //Page 109
        SET_D1(     0xCBCA, SetBitInRegister::bit1ofD), //Page 109
        SET_E1(     0xCBCB, SetBitInRegister::bit1ofE), //Page 109
        SET_H1(     0xCBCC, SetBitInRegister::bit1ofH), //Page 109
        SET_L1(     0xCBCD, SetBitInRegister::bit1ofL), //Page 109
        SET_1_HL(   0xCBCE, SetBitAddressOfHL::bit1), //Page 109
        SET_A1(     0xCBCF, SetBitInRegister::bit1ofA), //Page 109

        SET_B2(     0xCBD0, SetBitInRegister::bit2ofB), //Page 109
        SET_C2(     0xCBD1, SetBitInRegister::bit2ofC), //Page 109
        SET_D2(     0xCBD2, SetBitInRegister::bit2ofD), //Page 109
        SET_E2(     0xCBD3, SetBitInRegister::bit2ofE), //Page 109
        SET_H2(     0xCBD4, SetBitInRegister::bit2ofH), //Page 109
        SET_L2(     0xCBD5, SetBitInRegister::bit2ofL), //Page 109
        SET_2_HL(   0xCBD6, SetBitAddressOfHL::bit2), //Page 109
        SET_A2(     0xCBD7, SetBitInRegister::bit2ofA), //Page 109
        SET_B3(     0xCBD8, SetBitInRegister::bit3ofB), //Page 109
        SET_C3(     0xCBD9, SetBitInRegister::bit3ofC), //Page 109
        SET_D3(     0xCBDA, SetBitInRegister::bit3ofD), //Page 109
        SET_E3(     0xCBDB, SetBitInRegister::bit3ofE), //Page 109
        SET_H3(     0xCBDC, SetBitInRegister::bit3ofH), //Page 109
        SET_L3(     0xCBDD, SetBitInRegister::bit3ofL), //Page 109
        SET_3_HL(   0xCBDE, SetBitAddressOfHL::bit3), //Page 109
        SET_A3(     0xCBDF, SetBitInRegister::bit3ofA), //Page 109

        SET_B4(     0xCBE0, SetBitInRegister::bit4ofB), //Page 109
        SET_C4(     0xCBE1, SetBitInRegister::bit4ofC), //Page 109
        SET_D4(     0xCBE2, SetBitInRegister::bit4ofD), //Page 109
        SET_E4(     0xCBE3, SetBitInRegister::bit4ofE), //Page 109
        SET_H4(     0xCBE4, SetBitInRegister::bit4ofH), //Page 109
        SET_L4(     0xCBE5, SetBitInRegister::bit4ofL), //Page 109
        SET_4_HL(   0xCBE6, SetBitAddressOfHL::bit4), //Page 109
        SET_A4(     0xCBE7, SetBitInRegister::bit4ofA), //Page 109
        SET_B5(     0xCBE8, SetBitInRegister::bit5ofB), //Page 109
        SET_C5(     0xCBE9, SetBitInRegister::bit5ofC), //Page 109
        SET_D5(     0xCBEA, SetBitInRegister::bit5ofD), //Page 109
        SET_E5(     0xCBEB, SetBitInRegister::bit5ofE), //Page 109
        SET_H5(     0xCBEC, SetBitInRegister::bit5ofH), //Page 109
        SET_L5(     0xCBED, SetBitInRegister::bit5ofL), //Page 109
        SET_5_HL(   0xCBEE, SetBitAddressOfHL::bit5), //Page 109
        SET_A5(     0xCBEF, SetBitInRegister::bit5ofA), //Page 109

        SET_B6(     0xCBF0, SetBitInRegister::bit6ofB), //Page 109
        SET_C6(     0xCBF1, SetBitInRegister::bit6ofC), //Page 109
        SET_D6(     0xCBF2, SetBitInRegister::bit6ofD), //Page 109
        SET_E6(     0xCBF3, SetBitInRegister::bit6ofE), //Page 109
        SET_H6(     0xCBF4, SetBitInRegister::bit6ofH), //Page 109
        SET_L6(     0xCBF5, SetBitInRegister::bit6ofL), //Page 109
        SET_6_HL(   0xCBF6, SetBitAddressOfHL::bit6), //Page 109
        SET_A6(     0xCBF7, SetBitInRegister::bit6ofA), //Page 109
        SET_B7(     0xCBF8, SetBitInRegister::bit7ofB), //Page 109
        SET_C7(     0xCBF9, SetBitInRegister::bit7ofC), //Page 109
        SET_D7(     0xCBFA, SetBitInRegister::bit7ofD), //Page 109
        SET_E7(     0xCBFB, SetBitInRegister::bit7ofE), //Page 109
        SET_H7(     0xCBFC, SetBitInRegister::bit7ofH), //Page 109
        SET_L7(     0xCBFD, SetBitInRegister::bit7ofL), //Page 109
        SET_7_HL(   0xCBFE, SetBitAddressOfHL::bit7), //Page 109
        SET_A7(     0xCBFF, SetBitInRegister::bit7ofA); //Page 109

        private final static EnumByValue<InstructionType> valuesCache = new EnumByValue<>(values(), InstructionType.class);
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
            InstructionType type = valuesCache.fromValue(opcode);
            if (type != null) {
                return type;
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

        public int compareTo(int value) {
            return value - opcode;
        }
    }
}
