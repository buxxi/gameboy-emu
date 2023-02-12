package se.omfilm.gameboy.io.screen;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;
import se.omfilm.gameboy.io.color.Color;

import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL12.*;

public class GLFWScreen implements Screen {
    private static final int UPDATE_FPS_INTERVAL_SECONDS = 2;

    private final String title;
    private final WindowChangeListener windowChangeListener;
    private final Mode mode;

    private final AtomicBoolean turnedOn = new AtomicBoolean(false);
    private Window window;

    private final byte[] pixelBuffer = new byte[Screen.WIDTH * Screen.HEIGHT * 3];
    private final AtomicReference<byte[]> pixelBufferTransfer = new AtomicReference<>();

    private final FPSCounter fps = new FPSCounter();

    public GLFWScreen(String title, WindowChangeListener windowChangeListener, Mode mode) {
        this.title = title;
        this.windowChangeListener = windowChangeListener;
        this.mode = mode;
    }

    public void turnOn() {
        if (turnedOn.get()) {
            return;
        }
        if (!glfwInit()) {
            throw new IllegalStateException("Could not initialize GL");
        }

        turnedOn.set(true);
        Executors.newSingleThreadExecutor().execute(new GLWindowRenderer());
    }

    public void turnOff() {
        glfwTerminate();
        turnedOn.set(false);
    }

    public void setPixel(int x, int y, Color color) {
        pixelBuffer[index(x, y)] = color.getRed();
        pixelBuffer[index(x, y) + 1] = color.getGreen();
        pixelBuffer[index(x, y) + 2] = color.getBlue();
    }

    public void draw() {
        fps.incrementEngine();
        fps.update();
        pixelBufferTransfer.set(pixelBuffer);
    }

    private int index(int x, int y) {
        return 3 * ((y * Screen.WIDTH) + x);
    }

    private class GLWindowRenderer implements Runnable {
        public void run() {
            window = initializeWindow();
            initializeGL();
            Texture texture = initializeTexture();
            while (turnedOn.get()) {
                render(texture);
            }
        }

        private void render(Texture texture) {
            if (glfwWindowShouldClose(window.id())) {
                turnOff();

                System.exit(0);
            }

            fps.incrementRendered();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            updateTextureBufferIfNeeded(texture);
            renderTextureToQuad();

            glfwSwapBuffers(window.id());
            glfwPollEvents();
        }

        private void updateTextureBufferIfNeeded(Texture texture) {
            byte[] transferredBuffer = pixelBufferTransfer.getAndSet(null);
            if (transferredBuffer != null) {
                texture.buffer().put(transferredBuffer);
                texture.buffer().flip();
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, Screen.WIDTH, Screen.HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, texture.buffer());
            }
        }

        private void renderTextureToQuad() {
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex2f(0, 0);
            glTexCoord2f(0, 1);
            glVertex2f(0, Screen.HEIGHT);
            glTexCoord2f(1, 1);
            glVertex2f(Screen.WIDTH, Screen.HEIGHT);
            glTexCoord2f(1, 0);
            glVertex2f(Screen.WIDTH, 0);
            glEnd();
        }

        private Window initializeWindow() {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

            int windowWidth = mode.width();
            int windowHeight = mode.height();
            long monitor = mode.monitor();

            Window window = new Window(GLFW.glfwCreateWindow(windowWidth, windowHeight, title, monitor, MemoryUtil.NULL));
            if (window.id() == MemoryUtil.NULL) {
                throw new IllegalStateException("Could not initialize GLFW Window");
            }
            windowChangeListener.windowChanged(window);
            if (monitor == MemoryUtil.NULL) {
                GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
                glfwSetWindowPos(window.id(), (vidmode.width() - mode.width()) / 2, (vidmode.height() - mode.height()) / 2);
            }

            glfwMakeContextCurrent(window.id());
            glfwSwapInterval(1);
            glfwShowWindow(window.id());
            return window;
        }

        private void initializeGL() {
            GL.createCapabilities();
            glClearColor(0f, 0f, 0f, 1f);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(-mode.borderSize(), Screen.WIDTH + mode.borderSize(), Screen.HEIGHT, 0, 1, -1);
            glMatrixMode(GL_MODELVIEW);
        }

        private static Texture initializeTexture() {
            Texture texture = new Texture(glGenTextures(), BufferUtils.createByteBuffer(Screen.WIDTH * Screen.HEIGHT * 3));
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, texture.id());

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, Screen.WIDTH, Screen.HEIGHT, 0, GL_RGB, GL_UNSIGNED_BYTE, texture.buffer());
            return texture;
        }
    }

    private class FPSCounter {
        private long lastFPSUpdate = System.currentTimeMillis();
        private final AtomicInteger engineCounter = new AtomicInteger();
        private final AtomicInteger renderedCounter = new AtomicInteger();

        public void update() {
            long now = System.currentTimeMillis();
            if ((now - (UPDATE_FPS_INTERVAL_SECONDS * 1000)) > lastFPSUpdate) {
                GLFW.glfwSetWindowTitle(window.id(), title + ", engine fps: " + fps(engineCounter.get()) + ", display fps: " + (fps(renderedCounter.get())));
                lastFPSUpdate = now;
                engineCounter.set(0);
                renderedCounter.set(0);
            }
        }

        public void incrementEngine() {
            engineCounter.incrementAndGet();
        }

        public void incrementRendered() {
            renderedCounter.incrementAndGet();
        }

        private int fps(int counter) {
            return counter / UPDATE_FPS_INTERVAL_SECONDS;
        }
    }

    private record Texture(int id, ByteBuffer buffer) {}

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
