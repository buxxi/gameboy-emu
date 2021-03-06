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
        switch (button) {
            case UP:
                return checkKey(GLFW.GLFW_KEY_W);
            case DOWN:
                return checkKey(GLFW.GLFW_KEY_S);
            case LEFT:
                return checkKey(GLFW.GLFW_KEY_A);
            case RIGHT:
                return checkKey(GLFW.GLFW_KEY_D);
            case START:
                return checkKey(GLFW.GLFW_KEY_ENTER);
            case SELECT:
                return checkKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
            case A:
                return checkKey(GLFW.GLFW_KEY_O);
            case B:
                return checkKey(GLFW.GLFW_KEY_P);
            default:
                return false;
        }
    }

    public void update() {

    }

    private boolean checkKey(int keyId) {
        return windowSet && GLFW.glfwGetKey(window, keyId) == 1;
    }
}
