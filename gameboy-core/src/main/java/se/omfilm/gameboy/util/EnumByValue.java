package se.omfilm.gameboy.util;

import java.lang.reflect.Array;
import java.util.function.Consumer;

public class EnumByValue<T extends EnumByValue.ComparableByInt> {
    private final T[] values;
    private final Consumer<Integer> errorConsumer;

    private EnumByValue(T[] values, Class<T> clazz, Consumer<Integer> errorConsumer) {
        this.errorConsumer = errorConsumer;
        int max = max(values); //Find the max value, so we know how big the array needs to be
        this.values = fill(values, createArray(clazz, max)); //Populate the array so we just can lookup by the index
    }

    public static <T extends ComparableByInt> EnumByValue<T> create(T[] values, Class<T> clazz, Consumer<Integer> errorConsumer) {
        return new EnumByValue<>(values, clazz, errorConsumer);
    }

    public T fromValue(int value) {
        T result = values[value];
        if (result != null) {
            return result;
        }
        errorConsumer.accept(value);
        return null;
    }

    private int max(T[] values) {
        int i = 0;
        while (isAnyAbove(values, i)) {
            i++;
        }
        return i;
    }

    private T[] fill(T[] input, T[] result) {
        for (T i : input) {
            for (int j = 0; j < result.length; j++) {
                if (i.compareTo(j) == 0) {
                    result[j] = i;
                }
            }
        }
        return result;
    }

    private boolean isAnyAbove(T[] values, int i) {
        for (T val : values) {
            if (val.compareTo(i) <= 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private T[] createArray(Class<T> clazz, int max) {
        return (T[]) Array.newInstance(clazz, max);
    }

    public interface ComparableByInt {
        int compareTo(int value);
    }
}
