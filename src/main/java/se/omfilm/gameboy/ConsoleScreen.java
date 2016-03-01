package se.omfilm.gameboy;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ConsoleScreen implements Screen {
    private boolean written = false;
    private char[][] data;

    private Map<Color, Character> colorMapping = new HashMap<>();

    public void initialize(int width, int height) {
        data = new char[height][width];
        colorMapping.put(Color.BLACK, 'x');
        colorMapping.put(Color.DARK_GRAY, '+');
        colorMapping.put(Color.LIGHT_GRAY, 'o');
        colorMapping.put(Color.WHITE, ' ');
    }

    public void setPixel(int x, int y, Color color) {
        data[y][x] = colorMapping.get(color);
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
