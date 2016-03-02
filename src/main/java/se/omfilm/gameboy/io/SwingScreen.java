package se.omfilm.gameboy.io;

import se.omfilm.gameboy.Screen;

import javax.swing.*;
import java.awt.*;

public class SwingScreen extends JPanel implements Screen {
    private static final int SCALE = 4;
    private Color[][] data;
    private JFrame frame;
    private long last = 0;

    @Override
    public void initialize() {
        data = new Color[Screen.HEIGHT][Screen.WIDTH];
        frame = new JFrame();
        frame.setSize(Screen.WIDTH * SCALE, Screen.HEIGHT * SCALE);
        frame.add(this);
        frame.setVisible(true);
    }

    @Override
    public void setPixel(int x, int y, Color color) {
        try {
            data[y][x] = color;
        } catch (Exception e) {} //TODO: should never call this while index out of bounds
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;
        for (int y = 0; y < Screen.HEIGHT; y++) {
            for (int x = 0; x < Screen.WIDTH; x++) {
                g.setColor(data[y][x]);
                g.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
            }
        }
        int fps = (int) (1000 / Math.max(System.currentTimeMillis() - last, 1));
        last = System.currentTimeMillis();
        g.setColor(Color.RED);
        g.setFont(new Font("Verdana", Font.PLAIN, 9));
        g.drawString("fps: " + fps, 1, 10);
    }

    @Override
    public void draw() {
        frame.repaint();
    }
}
