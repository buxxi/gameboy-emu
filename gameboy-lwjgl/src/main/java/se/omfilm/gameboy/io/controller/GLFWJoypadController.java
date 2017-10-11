package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import se.omfilm.gameboy.io.screen.WindowChangeListener;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWJoypadController implements Controller, WindowChangeListener {
    private final int joypad;
    private final GLFWGamepadState state;

    public GLFWJoypadController(int joypad) {
        this.joypad = joypad;
        state = GLFWGamepadState.create();
    }

    public boolean isPressed(Button button) {

        switch (button) {
            case UP:
                return checkAxis(GLFW_GAMEPAD_AXIS_LEFT_Y, -1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_UP);
            case DOWN:
                return checkAxis(GLFW_GAMEPAD_AXIS_LEFT_Y, 1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_DOWN);
            case LEFT:
                return checkAxis(GLFW_GAMEPAD_AXIS_LEFT_X, -1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_LEFT);
            case RIGHT:
                return checkAxis(GLFW_GAMEPAD_AXIS_LEFT_X, 1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);
            case START:
                return checkButton(GLFW_GAMEPAD_BUTTON_START);
            case SELECT:
                return checkButton(GLFW_GAMEPAD_BUTTON_BACK);
            case A:
                return checkButton(GLFW_GAMEPAD_BUTTON_A);
            case B:
                return checkButton(GLFW_GAMEPAD_BUTTON_X) || checkButton(GLFW_GAMEPAD_BUTTON_B);
            default:
                return false;
        }
    }

    public void update() {
        GLFW.glfwGetGamepadState(joypad, state);
    }

    private boolean checkAxis(int axisIndex, int expectedValue) {
        return Math.round(state.axes(axisIndex)) == expectedValue;
    }

    private boolean checkButton(int buttonIndex) {
        return state.buttons(buttonIndex) == 1;
    }

    public void windowChanged(long window) {
    }
}
