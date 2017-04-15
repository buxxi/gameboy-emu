package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.PPU;

public class MultiColorPalette implements ColorPalette {
    private final ColorPalette tilePalette;
    private final ColorPalette windowPalette;
    private final ColorPalette sprite0Palette;
    private final ColorPalette sprite1Palette;

    public MultiColorPalette(ColorPalette backgroundPalette, ColorPalette windowPalette, ColorPalette sprite0Palette, ColorPalette sprite1Palette) {
        this.tilePalette = backgroundPalette;
        this.windowPalette = windowPalette;
        this.sprite0Palette = sprite0Palette;
        this.sprite1Palette = sprite1Palette;
    }

    public Color background(PPU.Shade shade) {
        return tilePalette.background(shade);
    }

    public Color window(PPU.Shade shade) {
        return windowPalette.window(shade);
    }

    public Color sprite(PPU.Shade shade, int index) {
        if (index == 0) {
            return sprite0Palette.sprite(shade, index);
        }
        return sprite1Palette.sprite(shade, index);
    }
}
