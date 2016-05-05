package se.omfilm.gameboy.io.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class SwingController implements Controller, KeyListener {
    private Set<Button> pressed = new HashSet<>();

    public boolean isPressed(Button button) {
        return pressed.contains(button);
    }

    public void keyReleased(KeyEvent keyEvent) {
        button(keyEvent).ifPresent(pressed::remove);
    }

    public void keyPressed(KeyEvent keyEvent) {
        button(keyEvent).ifPresent(pressed::add);
    }

    public void keyTyped(KeyEvent keyEvent) {

    }

    private Optional<Button> button(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_W:
                return Optional.of(Button.UP);
            case KeyEvent.VK_S:
                return Optional.of(Button.DOWN);
            case KeyEvent.VK_A:
                return Optional.of(Button.LEFT);
            case KeyEvent.VK_D:
                return Optional.of(Button.RIGHT);
            case KeyEvent.VK_BACK_SPACE:
                return Optional.of(Button.SELECT);
            case KeyEvent.VK_ENTER:
                return Optional.of(Button.START);
            case KeyEvent.VK_O:
                return Optional.of(Button.A);
            case KeyEvent.VK_P:
                return Optional.of(Button.B);
            default:
                return Optional.empty();
        }
    }
}
