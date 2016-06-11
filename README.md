# An emulator for the original GameBoy written in Java
This is my attempt at learning to write an emulator.
Made in Java since that's my primary language and I just want to learn about the emulation and not a new language.
![Super Mario Land Screenshot][screenshot]

### Goals
 - [x] Implement the engine separate so multiple implementations of screens, inputs etc can be made
 - [x] Support for switching out the color scheme
 - [x] Make an OpenGL implementation for the display
 - [x] Joypad support
 - [ ] Pass all test roms (see below)
 - [ ] Save game support
 - [ ] Sound support (maybe)
 - [ ] Simple GUI for loading ROMs and configure the emulator
 - [ ] Being able to play through a full game without any problems
 
### Non goals
 - Gameboy Color support
 - Save states
 - Fancy GUI
 - Serial connection support locally or over internet

### Blargg test roms
This is the current status of the unit tests that automatically runs [Blarggs test roms](http://blargg.8bitalley.com/parodius/gb-tests/). Haven't added all, only those that feels most relevant.
 - [ ] cpu_instrs:
     - [x] 01-special
     - [ ] 02-interrupts: (HALT broken again)
     - [x] 03-op sp,hl
     - [x] 04-op r,imm
     - [x] 05-op rp:
     - [x] 06-ld r,r
     - [x] 07-jr,jp,call,ret,rst
     - [x] 08-misc instrs
     - [x] 09-op r,r
     - [x] 10-bit ops
     - [x] 11-op a,(hl)
 - [x] instr_timing
 - [ ] mem_timing:
    - [ ] 01-read timing: Failed (BE:1-2 8E:1-2 A6:1-2 46:1-2 56:1-2 66:1-2 F2:1-2 0A:1-2 3A:1-2 F0:2-3 FA:2-4 CB 46:2-3 CB 4E:2-3 CB 56:1-3 CB 5E:2-3 CB 66:1-3 CB 6E:2-3 CB 76:2-3 CB 7E:2-3)
    - [ ] 02-write timing: Failed (36:2-3 71:1-2 74:1-2 E0:2-3 EA:2-4)
    - [ ] 03-modify timing: Failed (too much output to write here)

### References, Thanks!
 - http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
 - http://www.codeslinger.co.uk/pages/projects/gameboy/beginning.html
 - http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-The-CPU
 - http://gbdev.gg8.se/wiki/articles/Main_Page
 - https://github.com/taisel/GameBoy-Online

 [screenshot]: https://github.com/buxxi/gameboy-emu/blob/master/mario_screenshot.gif

