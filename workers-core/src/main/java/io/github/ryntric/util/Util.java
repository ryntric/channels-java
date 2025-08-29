package io.github.ryntric.util;

import io.github.ryntric.Constants;
import io.github.ryntric.EventFactory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * author: ryntric
 * date: 8/8/25
 * time: 10:12 PM
 **/

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

    public static int assertBatchSizeGreaterThanZero(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than zero");
        }
        return batchSize;
    }

    public static int log2(final int value) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(value) - 1;
    }

    public static <E> E[] fillEventBuffer(EventFactory<E> factory, E[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = factory.newEvent();
        }
        return buffer;
    }

    public static int wrappedIndex(long sequence, long mask) {
        return (int) (sequence & mask);
    }

    public static int wrapPaddedIndex(long sequence, long mask) {
        return Constants.ARRAY_PADDING + wrappedIndex(sequence, mask);
    }

    public static int wrappedBufferIndex(long sequence, long mask) {
        return wrappedIndex(sequence, mask) << 2;
    }

    public static int getByteBufferCapacity(int bufferSize) {
        long capacity = (long) bufferSize << 2;
        if (capacity > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Request capacity too large" + capacity);
        }
        return (int) capacity;
    }


    public static void checkConstraintOfClaimedValue(int value, int bufferSize) {
        if (((value - 1) | (bufferSize - value)) < 0) {
            throw new IllegalArgumentException("Claimed value " + value + " is invalid: must be between 1 and " + bufferSize);
        }
    }

}
