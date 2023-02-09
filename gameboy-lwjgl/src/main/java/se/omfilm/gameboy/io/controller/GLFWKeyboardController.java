package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

public class GLFWKeyboardController implements Controller, WindowChangeListener {
    private long window;
    private boolean windowSet = false;

    public void windowChanged(long window) {
        this.window = window;
        windowSet = true;
    }

    public boolean isPressed(Button button) {
        return switch (button) {
            case UP -> checkKey(GLFW.GLFW_KEY_W);
            case DOWN -> checkKey(GLFW.GLFW_KEY_S);
            case LEFT -> checkKey(GLFW.GLFW_KEY_A);
            case RIGHT -> checkKey(GLFW.GLFW_KEY_D);
            case START -> checkKey(GLFW.GLFW_KEY_ENTER);
            case SELECT -> checkKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
            case A -> checkKey(GLFW.GLFW_KEY_O);
            case B -> checkKey(GLFW.GLFW_KEY_P);
        };
    }

    public void update() {

    }

    private boolean checkKey(int keyId) {
        return windowSet && GLFW.glfwGetKey(window, keyId) == 1;
    }
}
