# An emulator for the original GameBoy written in Java
This is my attempt at learning to write an emulator.
Made in Java since that's my primary language and I just want to learn about the emulation and not a new language.
![Super Mario Land Screenshot][screenshot]

### Goals
 - [x] Implement the engine separate so multiple implementations of screens, inputs etc can be made
 - [x] Support for switching out the color scheme
 - [x] Make an OpenGL implementation for the display
 - [x] Joypad support
 - [x] Save game support
 - [x] Some kind of sound support
 - [x] Simple CLI for loading ROMs and configure the emulator
 - [ ] Pass all test roms (see below)
 - [ ] Being able to play through a full game without any problems
 
### Non goals
 - Gameboy Color support
 - Save states
 - Fancy GUI
 - Serial connection support locally or over internet

### Blargg test roms
This is the current status of the unit tests that automatically runs [Blarggs test roms](http://blargg.8bitalley.com/parodius/gb-tests/). Run the tests to get the actual errors.
 - [x] cpu_instrs
 - [x] instr_timing
 - [ ] mem_timing:
    - [ ] 01-read timing
    - [ ] 02-write timing
    - [ ] 03-modify timing
 - [ ] dmg_sound:
    - [x] 01-registers
    - [ ] 02-len ctr
    - [ ] 03-trigger
    - [ ] 04-sweep
    - [ ] 05-sweep details
    - [ ] 06-overflow on trigger
    - [ ] 07-len sweep period sync
    - [ ] 08-len ctr during power
    - [ ] 09-wave read while on
    - [ ] 10-wave trigger while on
    - [ ] 11-regs after power
    - [ ] 12-wave write while on
 - [ ] halt_bug
 - [ ] interrupt_time
 - [ ] oam_bug:
    - [ ] 1-lcd_sync
    - [x] 2-causes
    - [ ] 3-non_causes
    - [ ] 4-scanline_timing
    - [x] 5-timing_bug
    - [ ] 6-timing_no_bug
    - [ ] 7-timing_effect
    - [ ] 8-instr_effect

### References, Thanks!
 - http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
 - http://www.codeslinger.co.uk/pages/projects/gameboy/beginning.html
 - http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-The-CPU
 - http://gbdev.gg8.se/wiki/articles/Main_Page
 - https://github.com/taisel/GameBoy-Online

 [screenshot]: https://github.com/buxxi/gameboy-emu/blob/master/mario_screenshot.gif

