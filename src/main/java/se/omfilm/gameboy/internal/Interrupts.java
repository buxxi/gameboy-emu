package se.omfilm.gameboy.internal;

import se.omfilm.gameboy.internal.memory.MMU;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface Interrupts {
    int step(MMU memory);

    void enable(Interrupt... interrupts);

    void request(Interrupt... interrupts);

    boolean enabled(Interrupt interrupt);

    boolean requested(Interrupt interrupt);

    enum Interrupt {
        VBLANK( 0b0000_0001),
        LCD(    0b0000_0010),
        TIMER(  0b0000_0100),
        SERIAL( 0b0000_1000),
        JOYPAD( 0b0001_0000);

        public final int mask;

        Interrupt(int mask) {
            this.mask = mask;
        }

        public static Interrupt[] fromValue(int data) {
            Set<Interrupt> result = Arrays.asList(values()).stream().filter(
                    v -> (v.mask & data) != 0
            ).collect(Collectors.toSet());
            return result.toArray(new Interrupt[result.size()]);
        }

        public static int enabledToValue(Interrupts interrupts) {
            int result = 0;
            for (Interrupt interrupt : Interrupt.values()) {
                if (interrupts.enabled(interrupt)) {
                    result = result | interrupt.mask;
                }
            }
            return result;
        }

        public static int requestedToValue(Interrupts interrupts) {
            int result = 0;
            for (Interrupt interrupt : Interrupt.values()) {
                if (interrupts.requested(interrupt)) {
                    result = result | interrupt.mask;
                }
            }
            return result;
        }
    }
}
