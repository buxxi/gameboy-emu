package se.omfilm.gameboy.io.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWScreen implements Screen {
    private static final int SCALE = 4;
    private static final int UPDATE_FPS_INTERVAL_SECONDS = 2;

    private final String title;
    private final WindowChangeListener windowChangeListener;

    private boolean turnedOn = false;
    private long window;
    private byte[] pixelBuffer = new byte[Screen.WIDTH * Screen.HEIGHT * 3];

    private long lastFPSUpdate = 0;
    private int frameCounter = 0;

    public GLFWScreen(String title, WindowChangeListener windowChangeListener) {
        this.title = title;
        this.windowChangeListener = windowChangeListener;
    }

    public void turnOn() {
        if (glfwInit() != GL11.GL_TRUE) {
            throw new IllegalStateException("Could not initialize GL");
        }

        initializeWindow();
        initializeGL();
        turnedOn = true;
    }

    public void turnOff() {
        glfwTerminate();
        turnedOn = false;
    }

    public void setPixel(int x, int y, Color color) {
        int index = index(x, y);
        pixelBuffer[index] = (byte) color.getRed();
        pixelBuffer[index + 1] = (byte) color.getGreen();
        pixelBuffer[index + 2] = (byte) color.getBlue();
    }

    public void draw() {
        if (glfwWindowShouldClose(window) == GLFW_TRUE) {
            turnOff();

            System.exit(0);
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        for (int y = 0; y < Screen.HEIGHT; y++) {
            for (int x = 0; x < Screen.WIDTH; x++) {
                int index = index(x, y);
                GL11.glColor3ub(pixelBuffer[index], pixelBuffer[index + 1], pixelBuffer[index + 2]);
                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(x, y);
                GL11.glVertex2f(x, y + 1);
                GL11.glVertex2f(x + 1, y + 1);
                GL11.glVertex2f(x + 1, y);
                GL11.glEnd();
            }
        }

        updateFPS();

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public boolean isOn() {
        return turnedOn;
    }

    private void initializeGL() {
        GL.createCapabilities();
        GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, Screen.WIDTH, Screen.HEIGHT, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void initializeWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        int windowWidth = WIDTH * SCALE;
        int windowHeight = HEIGHT * SCALE;
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new IllegalStateException("Could not initialize GLFW Window");
        }
        windowChangeListener.windowChanged(window);

        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - windowWidth) / 2, (vidmode.height()- windowHeight) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);
    }

    private void updateFPS() {
        frameCounter++;
        long now = System.currentTimeMillis();
        if ((now - (UPDATE_FPS_INTERVAL_SECONDS * 1000)) > lastFPSUpdate) {
            GLFW.glfwSetWindowTitle(window, title + ", fps: " + (frameCounter / UPDATE_FPS_INTERVAL_SECONDS));
            lastFPSUpdate = now;
            frameCounter = 0;
        }
    }

    private int index(int x, int y) {
        return 3 * ((y * Screen.WIDTH) + x);
    }
}
