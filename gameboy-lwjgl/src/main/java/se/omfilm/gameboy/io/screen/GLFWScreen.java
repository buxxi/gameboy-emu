package se.omfilm.gameboy.io.screen;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import se.omfilm.gameboy.io.color.Color;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWScreen implements Screen {
    private static final int UPDATE_FPS_INTERVAL_SECONDS = 2;

    private final String title;
    private final WindowChangeListener windowChangeListener;
    private final Mode mode;

    private volatile boolean turnedOn = false;
    private long window;

    private ReadWriteLock bufferLock = new ReentrantReadWriteLock();
    private byte[] pixelBuffer = new byte[Screen.WIDTH * Screen.HEIGHT * 3];
    private byte[] offscreenBuffer = new byte[pixelBuffer.length];

    private final FPSCounter fps = new FPSCounter();

    public GLFWScreen(String title, WindowChangeListener windowChangeListener, Mode mode) {
        this.title = title;
        this.windowChangeListener = windowChangeListener;
        this.mode = mode;
    }

    public void turnOn() {
        if (turnedOn) {
            return;
        }
        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GL");
        }

        turnedOn = true;
        Executors.newSingleThreadExecutor().execute(() -> {
            initializeWindow();
            initializeGL();
            while (turnedOn) {
                render();
            }
        });
    }

    public void turnOff() {
        glfwTerminate();
        turnedOn = false;
    }

    public void setPixel(int x, int y, Color color) {
        int index = index(x, y);
        offscreenBuffer[index] = color.getRed();
        offscreenBuffer[index + 1] = color.getGreen();
        offscreenBuffer[index + 2] = color.getBlue();
    }

    public void draw() {
        fps.engineCounter++;
        fps.update();

        bufferLock.writeLock().lock();
        try {
            switchBuffer();
        } finally {
            bufferLock.writeLock().unlock();
        }
    }

    private void switchBuffer() {
        byte[] tmp = pixelBuffer;
        pixelBuffer = offscreenBuffer;
        offscreenBuffer = tmp;
    }

    private void render() {
        if (glfwWindowShouldClose(window)) {
            turnOff();

            System.exit(0);
        }

        fps.renderedCounter++;

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        bufferLock.readLock().lock();
        try {
            renderToGL();
        } finally {
            bufferLock.readLock().unlock();
        }

        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    private void renderToGL() {
        GL11.glBegin(GL11.GL_QUADS);
        for (int y = 0; y < Screen.HEIGHT; y++) {
            for (int x = 0; x < Screen.WIDTH; x++) {
                int index = index(x, y);
                GL11.glColor3ub(pixelBuffer[index], pixelBuffer[index + 1], pixelBuffer[index + 2]);
                GL11.glVertex2f(x, y);
                GL11.glVertex2f(x, y + 1);
                GL11.glVertex2f(x + 1, y + 1);
                GL11.glVertex2f(x + 1, y);
            }
        }
        GL11.glEnd();
    }

    private void initializeGL() {
        GL.createCapabilities();
        GL11.glClearColor(0f, 0f, 0f, 1f);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-mode.borderSize(), Screen.WIDTH + mode.borderSize(), Screen.HEIGHT, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void initializeWindow() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        int windowWidth = mode.width();
        int windowHeight = mode.height();
        long monitor = mode.monitor();

        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, title, monitor, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new IllegalStateException("Could not initialize GLFW Window");
        }
        windowChangeListener.windowChanged(window);
        if (monitor == MemoryUtil.NULL) {
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - mode.width()) / 2, (vidmode.height() - mode.height()) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private int index(int x, int y) {
        return 3 * ((y * Screen.WIDTH) + x);
    }

    private class FPSCounter {
        private long lastFPSUpdate = System.currentTimeMillis();
        private volatile int engineCounter = 0;
        private volatile int renderedCounter = 0;

        public void update() {
            long now = System.currentTimeMillis();
            if ((now - (UPDATE_FPS_INTERVAL_SECONDS * 1000)) > lastFPSUpdate) {
                GLFW.glfwSetWindowTitle(window, title + ", engine fps: " + fps(engineCounter) + ", display fps: " + (fps(renderedCounter)));
                lastFPSUpdate = now;
                engineCounter = 0;
                renderedCounter = 0;
            }
        }

        private int fps(int counter) {
            return counter / UPDATE_FPS_INTERVAL_SECONDS;
        }
    }

    public enum Mode {
        FULLSCREEN(0) {
            protected long monitor() {
                return glfwGetPrimaryMonitor();
            }

            protected int height() {
                return glfwGetVideoMode(monitor()).height();
            }

            protected int width() {
                return glfwGetVideoMode(monitor()).width();
            }

            protected int borderSize() {
                float aspectRatioDiff = (((float) width() / height()) - ((float) Screen.WIDTH / Screen.HEIGHT));
                return (int) (aspectRatioDiff * Screen.HEIGHT / 2);
            }
        },
        FULLSCREEN_STRETCH(0) {
            protected long monitor() {
                return glfwGetPrimaryMonitor();
            }

            protected int height() {
                return glfwGetVideoMode(monitor()).height();
            }

            protected int width() {
                return glfwGetVideoMode(monitor()).width();
            }
        },
        SCALE_1X(1),
        SCALE_2X(2),
        SCALE_4X(4);

        private final int scale;

        Mode(int scale) {
            this.scale = scale;
        }

        protected int width() {
            return Screen.WIDTH * scale;
        }

        protected int height() {
            return Screen.HEIGHT * scale;
        }

        protected long monitor() {
            return MemoryUtil.NULL;
        }

        protected int borderSize() {
            return 0;
        }
    }
}
