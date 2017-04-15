package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GLFWJoypadController implements Controller, WindowChangeListener {
    private final int joypad;

    public GLFWJoypadController(int joypad) {
        this.joypad = joypad;
    }

    public boolean isPressed(Button button) {
        switch (button) {
            case UP:
                return checkAxis(7, -1) || checkAxis(1, -1);
            case DOWN:
                return checkAxis(7, 1) || checkAxis(1, 1);
            case LEFT:
                return checkAxis(6, -1) || checkAxis(0, -1);
            case RIGHT:
                return checkAxis(6, 1) || checkAxis(0, 1);
            case START:
                return checkButton(7);
            case SELECT:
                return checkButton(6);
            case A:
                return checkButton(0);
            case B:
                return checkButton(1) || checkButton(2);
            default:
                return false;
        }
    }

    private boolean checkAxis(int axisIndex, int expectedValue) {
        FloatBuffer buffer = GLFW.glfwGetJoystickAxes(joypad);
        return Math.round(buffer.get(axisIndex)) == expectedValue;
    }

    private boolean checkButton(int buttonIndex) {
        ByteBuffer buffer = GLFW.glfwGetJoystickButtons(joypad);
        return buffer.get(buttonIndex) == 1;
    }

    public void windowChanged(long window) {
    }
}
