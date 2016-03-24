# An emulator for the original GameBoy written in Java
My attempt at learning to write an emulator

### CPU Tests (blargg test roms)
__Current status:__
 - 01-special: __Passed__
 - 02-interrupts: Failed #2 (EI)
 - 03-op sp,hl: Failed (33 39 E8 E8 F8 F8)
 - 04-op r,imm: Failed (DE E6)
 - 05-op rp: Failed (03 13 23)
 - 06-ld r,r: __Passed__
 - 07-jr,jp,call,ret,rst: __Passed__
 - 08-misc instrs: __Passed__
 - 09-op r,r: Failed (Too many to write here...)
 - 10-bit ops: Crash (Unimplemented instructions)
 - 11-op a,(hl): Failed (Too many to write here...)