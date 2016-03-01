package se.omfilm.gameboy.io;

import se.omfilm.gameboy.Screen;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConsoleScreen implements Screen {
    private boolean written = false;
    private char[][] data;

    private Map<Color, Character> colorMapping = new HashMap<>();

    public void initialize() {
        data = new char[Screen.HEIGHT][Screen.WIDTH];
        colorMapping.put(Color.BLACK, 'x');
        colorMapping.put(Color.DARK_GRAY, '+');
        colorMapping.put(Color.LIGHT_GRAY, 'o');
        colorMapping.put(Color.WHITE, ' ');
    }

    public void setPixel(int x, int y, Color color) {
        try {
            data[y][x] = colorMapping.get(color);
        } catch (Exception e) {} //TODO: should never call this while index out of bounds
    }

    @Override
    public void draw() {
        if (!written) {
            written = true;
            for (int y = 0; y < data.length; y++) {
                for (int x = 0; x < data[y].length; x++) {
                    System.out.print(data[y][x]);
                }
                System.out.println();
            }
        }
    }
}
