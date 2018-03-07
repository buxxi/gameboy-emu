package se.omfilm.gameboy.internal.memory;

/**
 * Represents a physical cartridge of a game.
 * It's basically just a Memory, some implementations can have banks of ROM/RAM where writing to a special address changes which bank is used.
 * The Cartridge needs to know about the emulated time for the MBC3-implementation to simulate a RealTimeClock,
 * since the emulator can run in different speeds this needs to be based on emulated time and not the system time.
 */
public interface Cartridge extends Memory {
    void step(int cycles);
}
