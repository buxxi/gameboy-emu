package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.PPU.Shade;

public interface ColorPalette {
    Color window(Shade shade);

    Color background(Shade shade);

    Color sprite(Shade shade, int index);
}
