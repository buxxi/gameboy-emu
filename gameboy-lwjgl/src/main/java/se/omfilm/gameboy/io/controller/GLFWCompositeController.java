package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import se.omfilm.gameboy.io.screen.Screen;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

import java.util.stream.IntStream;

public class GLFWCompositeController implements Controller, WindowChangeListener {
    private final GLFWKeyboardController keyboard;
    private GLFWJoypadController[] joypads = new GLFWJoypadController[0];
    private long lastCheck = System.currentTimeMillis();

    public GLFWCompositeController() {
        keyboard = new GLFWKeyboardController();
    }

    public boolean isPressed(Button button) {
        return keyboard.isPressed(button) || anyJoypadPressed(button);
    }

    private boolean anyJoypadPressed(Button button) {
        for (int i = 0; i < joypads.length; i++) {
            if (joypads[i].isPressed(button)) {
                return true;
            }
        }
        return false;
    }

    public void update() {
        if (System.currentTimeMillis() - lastCheck <= (Screen.FREQUENCY / 60)) { //Only perform the expensive check once every frame
            return;
        }
        for (int i = 0; i < joypads.length; i++) {
            joypads[i].update();
        }
        lastCheck = System.currentTimeMillis();
    }

    public void windowChanged(long window) {
        keyboard.windowChanged(window);
        joypads = IntStream.range(GLFW.GLFW_JOYSTICK_1, GLFW.GLFW_JOYSTICK_LAST + 1).
                filter(GLFW::glfwJoystickPresent).
                mapToObj(GLFWJoypadController::new).
                toArray(GLFWJoypadController[]::new);
    }
}
