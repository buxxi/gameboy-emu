package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.GPU.Shade;

import java.awt.*;

public class FixedColorPalette implements ColorPalette {
    private final Color darkest;
    private final Color dark;
    private final Color light;
    private final Color lightest;

    private FixedColorPalette(Color darkest, Color dark, Color light, Color lightest) {
        this.darkest = darkest;
        this.dark = dark;
        this.light = light;
        this.lightest = lightest;
    }

    public Color background(Shade shade) {
        switch (shade) {
            case DARKEST:
                return darkest;
            case DARK:
                return dark;
            case LIGHT:
                return light;
            default:
                return lightest;
        }
    }

    public Color sprite(Shade shade, int index) {
        return background(shade);
    }

    public static ColorPalette monochrome() {
        return new FixedColorPalette(Color.BLACK, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.WHITE);
    }

    public static ColorPalette original() {
        return new FixedColorPalette(new Color(24, 60, 21), new Color(54, 100, 50), new Color(138, 174, 0), new Color(153, 189, 0));
    }

    public static ColorPalette green() {
        return new FixedColorPalette(new Color(4, 27, 35), new Color(53, 102, 81), new Color(135, 192, 123), new Color(224, 251, 210));
    }

    public static ColorPalette red() {
        return new FixedColorPalette(new Color(11, 33, 3), new Color(98, 102, 53), new Color(191, 145, 124), new Color(249, 215, 209));
    }

    public static ColorPalette purple() {
        return new FixedColorPalette(new Color(33, 11, 3), new Color(102, 53, 74), new Color(178, 124, 191), new Color(235, 209, 249));
    }

    public static ColorPalette blue() {
        return new FixedColorPalette(new Color(25, 3, 33), new Color(56, 53, 102), new Color(124, 170, 191), new Color(209, 243, 249));
    }
}
