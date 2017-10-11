package se.omfilm.gameboy.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.omfilm.gameboy.io.controller.Controller;

public class Input {
    private static final Logger log = LoggerFactory.getLogger(Input.class);

    private final Controller controller;

    private boolean checkDirections = false;
    private boolean checkButtons = false;
    private int controllerState = 0b0000_1111;

    public Input(Controller controller) {
        this.controller = controller;
    }

    public void step(int cycles, Interrupts interrupts) {
        int controllerState = this.controllerState;
        controller.update();
        if (checkButtons) {
            controllerState = buttonsState();
        } else if (checkDirections) {
            controllerState = directionsState();
        }

        if (this.controllerState != controllerState) {
            this.controllerState = controllerState;
            interrupts.request(Interrupts.Interrupt.JOYPAD, true);
        }
    }

    public int readState() {
        return controllerState;
    }

    public void writeState(int data) {
        checkButtons =      (data & 0b0010_0000) == 0;
        checkDirections =   (data & 0b0001_0000) == 0;
        if (checkButtons && checkDirections) {
            log.warn("Both buttons and direction is being checked, should not happen");
        }
    }

    private int directionsState() {
        return 0b0000_1111
                & (controller.isPressed(Controller.Button.DOWN) ? 0b0000_0111 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.UP) ? 0b0000_1011 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.LEFT) ? 0b0000_1101 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.RIGHT) ? 0b0000_1110 : 0b0000_1111);
    }

    private int buttonsState() {
        return 0b0000_1111
                & (controller.isPressed(Controller.Button.START) ? 0b0000_0111 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.SELECT) ? 0b0000_1011 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.B) ? 0b0000_1101 : 0b0000_1111)
                & (controller.isPressed(Controller.Button.A) ? 0b0000_1110 : 0b0000_1111);
    }
}
