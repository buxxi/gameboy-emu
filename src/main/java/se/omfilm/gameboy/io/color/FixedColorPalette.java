package se.omfilm.gameboy.io.color;

import se.omfilm.gameboy.internal.PPU.Shade;

import java.awt.*;

public class FixedColorPalette implements ColorPalette {
    private final Color darkest;
    private final Color dark;
    private final Color light;
    private final Color lightest;

    public FixedColorPalette(Color darkest, Color dark, Color light, Color lightest) {
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

    public enum PRESET {
        MONOCHROME(Color.BLACK, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.WHITE),
        ORIGINAL(new Color(24, 60, 21), new Color(54, 100, 50), new Color(138, 174, 0), new Color(153, 189, 0)),
        ORIGINAL_GREEN(new Color(4, 27, 35), new Color(53, 102, 81), new Color(135, 192, 123), new Color(224, 251, 210)),
        RED(new Color(85, 0, 0), new Color(128, 21, 21), new Color(212, 106, 106), new Color(255, 170, 170)),
        PINK(new Color(68, 0, 40), new Color(101, 17, 66), new Color(169, 84, 134), new Color(203, 135, 175)),
        PURPLE(new Color(38, 3, 57), new Color(62, 18, 85), new Color(119, 74, 142), new Color(152, 116, 170)),
        BLUE(new Color(6, 21, 57), new Color(22, 41, 85), new Color(79, 98, 142), new Color(120, 135, 171)),
        CYAN(new Color(0, 51, 51), new Color(13, 77, 77), new Color(34, 102, 102), new Color(102, 153, 153)),
        GREEN(new Color(0, 67, 4), new Color(17, 100, 22), new Color(84, 167, 89), new Color(134, 201, 138)),
        LIME(new Color(51, 79, 0), new Color(83, 118, 20), new Color(162, 197, 99), new Color(209, 237, 158)),
        YELLOW(new Color(85, 83, 0), new Color(128, 125, 21), new Color(212, 210, 106), new Color(255, 253, 170)),
        ORANGE(new Color(85, 40, 0), new Color(128, 71, 21), new Color(212, 156, 106), new Color(255, 210, 170));

        private final FixedColorPalette palette;

        PRESET(Color darkest, Color dark, Color light, Color lightest) {
            palette = new FixedColorPalette(darkest, dark, light, lightest);
        }

        public ColorPalette getPalette() {
            return palette;
        }
    }
}
