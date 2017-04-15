package se.omfilm.gameboy.internal.instructions.arithmetic;

import se.omfilm.gameboy.internal.*;
import se.omfilm.gameboy.internal.memory.Memory;

//Mostly based of https://github.com/taisel/GameBoy-Online and http://www.z80.info/z80syntx.htm#DAA
public class DecimalAdjustA implements Instruction {
    public int execute(Memory memory, Registers registers, Flags flags, ProgramCounter programCounter, StackPointer stackPointer) {
        int a = registers.readA();

        if (!flags.isSet(Flags.Flag.SUBTRACT)) {
            if (flags.isSet(Flags.Flag.CARRY) || a > 0x99) {
                a += 0x60;
                flags.set(Flags.Flag.CARRY, true);
            }

            if (flags.isSet(Flags.Flag.HALF_CARRY) || (a & 0x0F) > 0x09) {
                a += 0x06;
                flags.set(Flags.Flag.HALF_CARRY, false);
            }
        } else if (flags.isSet(Flags.Flag.CARRY) && flags.isSet(Flags.Flag.HALF_CARRY)) {
            a += 0x9A; //NEG
            flags.set(Flags.Flag.HALF_CARRY, false);
        } else if (flags.isSet(Flags.Flag.CARRY)) {
            a += 0xA0; //DEC
        } else if (flags.isSet(Flags.Flag.HALF_CARRY)) {
            a += 0xFA; //SBC
            flags.set(Flags.Flag.HALF_CARRY, false);
        }

        int result = a & 0xFF;

        flags.set(Flags.Flag.ZERO, result == 0);

        registers.writeA(result);

        return 4;
    }
}
