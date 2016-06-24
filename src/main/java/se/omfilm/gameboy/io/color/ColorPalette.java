package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.PPU.Shade;

/**
 * Map the shade of a pixel to a actual color.
 * The gameboy only has 4 shades, but knowing the type that is rendered we can translate it to different colors.
 * Or an implementation could just delegate all calls to background to make it same for all.
 */
public interface ColorPalette {
    Color window(Shade shade);

    Color background(Shade shade);

    Color sprite(Shade shade, int index);
}
