package se.omfilm.gameboy;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface Interrupts {
    void enable(Interrupt... interrupts);

    void request(Interrupt... interrupts);

    boolean enabled(Interrupt interrupt);

    enum Interrupt {
        VBLANK( 0b0000_0001),
        LCD(    0b0000_0010),
        TIMER(  0b0000_0100),
        JOYPAD( 0b0000_1000);

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

        public static int toValue(Interrupts interrupts) {
            int result = 0;
            for (Interrupt interrupt : Interrupt.values()) {
                if (interrupts.enabled(interrupt)) {
                    result = result | interrupt.mask;
                }
            }
            return result;
        }
    }
}
