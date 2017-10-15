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
See [COMPABILITY.md](COMPABILITY.md) for a detailed report how well this emulator passes the Blargg test roms

### References, Thanks!
 - http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf
 - http://www.codeslinger.co.uk/pages/projects/gameboy/beginning.html
 - http://imrannazar.com/GameBoy-Emulation-in-JavaScript:-The-CPU
 - http://gbdev.gg8.se/wiki/articles/Main_Page
 - https://github.com/taisel/GameBoy-Online

 [screenshot]: https://github.com/buxxi/gameboy-emu/blob/master/mario_screenshot.gif

