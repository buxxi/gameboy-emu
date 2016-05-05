package se.omfilm.gameboy.io.screen;

import se.omfilm.gameboy.util.DebugPrinter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class SwingScreen extends JPanel implements Screen {
    private static final int SCALE = 4;
    private Color[][] data;
    private JFrame frame;
    private long last = 0;

    public SwingScreen() {
        frame = new JFrame();
    }

    public void turnOn() {
        data = new Color[Screen.HEIGHT][Screen.WIDTH];
        this.setPreferredSize(new Dimension(Screen.WIDTH * SCALE, Screen.HEIGHT * SCALE));
        frame.add(this);
        frame.setVisible(true);
        frame.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                DebugPrinter.debugCallStack();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });
        frame.pack();
    }

    public void turnOff() {
        frame.dispose();
    }

    @Override
    public boolean isOn() {
        return data != null;
    }

    public void setPixel(int x, int y, Color color) {
        data[y][x] = color;
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
        g.setFont(new Font("Verdana", Font.PLAIN, 13));
        g.drawString("fps: " + fps, 1, 14);
    }

    @Override
    public synchronized void addKeyListener(KeyListener keyListener) {
        frame.addKeyListener(keyListener);
    }

    public void draw() {
        frame.repaint();
    }
}
