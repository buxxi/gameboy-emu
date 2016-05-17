# An emulator for the original GameBoy written in Java
My attempt at learning to write an emulator

### CPU Tests (blargg test roms)
__Current status:__
 - cpu_instrs:
     - 01-special: __Passed__
     - 02-interrupts: (HALT broken again)
     - 03-op sp,hl: __Passed__
     - 04-op r,imm: __Passed__
     - 05-op rp: __Passed__
     - 06-ld r,r: __Passed__
     - 07-jr,jp,call,ret,rst: __Passed__
     - 08-misc instrs: __Passed__
     - 09-op r,r: Failed __Passed__
     - 10-bit ops: __Passed__
     - 11-op a,(hl): __Passed__
 - instr_timing: __Passed__
 - mem_timing:
    - 01-read timing: Failed (BE:1-2 8E:1-2 A6:1-2 46:1-2 56:1-2 66:1-2 F2:1-2 0A:1-2 3A:1-2 F0:2-3 FA:2-4 CB 46:2-3 CB 4E:2-3 CB 56:1-3 CB 5E:2-3 CB 66:1-3 CB 6E:2-3 CB 76:2-3 CB 7E:2-3)
    - 02-write timing: Failed (36:2-3 71:1-2 74:1-2 E0:2-3 EA:2-4)
    - 03-modify timing: Failed (too much output to write here)
