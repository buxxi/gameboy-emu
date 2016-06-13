package se.omfilm.gameboy.util;

public class EnumByValue<T extends EnumByValue.ComparableByInt> {
    private final Object[] values;

    public EnumByValue(T[] values) {
        int max = max(values); //Find the max value, so we know how big the array needs to be
        this.values = fill(values, new Object[max]); //Populate the array so we just can lookup by the index
    }

    public T fromValue(int value) {
        return (T) values[value];
    }

    private int max(T[] values) {
        int i = 0;
        while (isAnyAbove(values, i)) {
            i++;
        }
        return i;
    }

    private Object[] fill(T[] input, Object[] result) {
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

    public interface ComparableByInt {
        int compareTo(int value);
    }
}
