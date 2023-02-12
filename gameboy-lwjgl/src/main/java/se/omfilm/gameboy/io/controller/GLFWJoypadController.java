package se.omfilm.gameboy.io.controller;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import se.omfilm.gameboy.io.screen.Window;
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
        return switch (button) {
            case UP -> checkAxis(GLFW_GAMEPAD_AXIS_LEFT_Y, -1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_UP);
            case DOWN -> checkAxis(GLFW_GAMEPAD_AXIS_LEFT_Y, 1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_DOWN);
            case LEFT -> checkAxis(GLFW_GAMEPAD_AXIS_LEFT_X, -1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_LEFT);
            case RIGHT -> checkAxis(GLFW_GAMEPAD_AXIS_LEFT_X, 1) || checkButton(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT);
            case START -> checkButton(GLFW_GAMEPAD_BUTTON_START);
            case SELECT -> checkButton(GLFW_GAMEPAD_BUTTON_BACK);
            case A -> checkButton(GLFW_GAMEPAD_BUTTON_A);
            case B -> checkButton(GLFW_GAMEPAD_BUTTON_X) || checkButton(GLFW_GAMEPAD_BUTTON_B);
        };
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

    public void windowChanged(Window window) {
    }
}
