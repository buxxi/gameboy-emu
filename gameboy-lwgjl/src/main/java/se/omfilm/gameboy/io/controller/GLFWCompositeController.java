package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GLFWCompositeController implements Controller, WindowChangeListener {
    private final GLFWKeyboardController keyboard;
    private Collection<Controller> joypads = Collections.emptyList();

    public GLFWCompositeController() {
        keyboard = new GLFWKeyboardController();
    }

    public boolean isPressed(Button button) {
        return keyboard.isPressed(button) || anyJoypadPressed(button);
    }

    private boolean anyJoypadPressed(Button button) {
        for (Controller controller : joypads) {
            if (controller.isPressed(button)) {
                return true;
            }
        }
        return false;
    }

    public void windowChanged(long window) {
        keyboard.windowChanged(window);
        joypads = IntStream.range(GLFW.GLFW_JOYSTICK_1, GLFW.GLFW_JOYSTICK_LAST + 1).
                filter(i -> GLFW.glfwJoystickPresent(i) == 1).
                mapToObj(GLFWJoypadController::new).
                collect(Collectors.toList());
    }
}