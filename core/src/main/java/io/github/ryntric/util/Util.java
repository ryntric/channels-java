package io.github.ryntric.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class Util {
    private Util() {}

    public static VarHandle findVarHandlePrivate(Class<?> clazz, String name, Class<?> type) {
        return ThrowableSupplier.sneaky(() -> MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                .findVarHandle(clazz, name, type));
    }

    public static boolean isPowerOfTwo(int n) {
        return Integer.bitCount(n) == 1;
    }

    public static int assertThatPowerOfTwo(int n) {
        if (!isPowerOfTwo(n)) {
            throw new IllegalArgumentException("Should be power of two");
        }
        return n;
    }

    public static int log2(int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
    }

    public static int wrapIndex(long sequence, long mask) {
        return (int) wrapLongIndex(sequence, mask);
    }

    public static long wrapLongIndex(long sequence, long mask) {
        return sequence & mask;
    }

}
