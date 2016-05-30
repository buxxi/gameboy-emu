package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.PPU.Shade;

import java.awt.*;

public interface ColorPalette {
    Color background(Shade shade);

    Color sprite(Shade shade, int index);
}
