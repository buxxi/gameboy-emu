package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

import java.util.stream.IntStream;

public class GLFWCompositeController implements Controller, WindowChangeListener {
    private final GLFWKeyboardController keyboard;
    private Controller[] joypads = new Controller[0];

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
        for (int i = 0; i < joypads.length; i++) {
            joypads[i].update();
        }
    }

    public void windowChanged(long window) {
        keyboard.windowChanged(window);
        joypads = IntStream.range(GLFW.GLFW_JOYSTICK_1, GLFW.GLFW_JOYSTICK_LAST + 1).
                filter(GLFW::glfwJoystickPresent).
                mapToObj(GLFWJoypadController::new).
                toArray(Controller[]::new);
    }
}
