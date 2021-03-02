package com.moez.QKSMS.common.util;

@SuppressWarnings("unused")
public class Arithmetics {

    /**
     * Increment {@code original} by the quantity of {@code incrementBy} with integer overflow handled.
     * In case of an overflow, return value is folded to restart from 0 as if it was an unsigned integer.
     * <p>
     * Both parameters should be non-negative, or return value is undefined.
     */
    public static int unsignedIncrement(int original, int incrementBy) {
        int maxIncrementWithoutOverflow = Integer.MAX_VALUE - original;
        if (incrementBy > maxIncrementWithoutOverflow) {
            return incrementBy - maxIncrementWithoutOverflow - 1;
        }
        return original + incrementBy;
    }
}
