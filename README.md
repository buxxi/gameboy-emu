# An emulator for the original GameBoy written in Java
My attempt at learning to write an emulator

### CPU Tests (blargg test roms)
__Current status:__
 - 01-special: __Passed__
 - 02-interrupts: Failed #2 (EI)
 - 03-op sp,hl: Failed (E8 F8)
 - 04-op r,imm: __Passed__
 - 05-op rp: __Passed__
 - 06-ld r,r: __Passed__
 - 07-jr,jp,call,ret,rst: __Passed__
 - 08-misc instrs: __Passed__
 - 09-op r,r: Failed __Passed__
 - 10-bit ops: Crash (Unimplemented instructions)
 - 11-op a,(hl): __Passed__