package se.omfilm.gameboy.util;

public class EnumByValue<T extends EnumByValue.ComparableByInt> {
    private final T[] values;

    public EnumByValue(T[] values) {
        this.values = values;
    }

    public T fromValue(int value) {
        return binarySearch(value, 0, values.length - 1);
    }

    private T binarySearch(int searchValue, int left, int right) {
        if (right < left) {
            return null;
        }

        int mid = (left + right) >>> 1;
        int diff = values[mid].compareTo(searchValue);
        if (diff > 0) {
            return binarySearch(searchValue, mid + 1, right);
        } else if (diff < 0) {
            return binarySearch(searchValue, left, mid - 1);
        } else {
            return values[mid];
        }
    }

    public interface ComparableByInt {
        int compareTo(int value);
    }
}
