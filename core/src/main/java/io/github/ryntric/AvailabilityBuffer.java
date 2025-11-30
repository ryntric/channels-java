package io.github.ryntric;

import io.github.ryntric.util.UnsafeUtil;
import io.github.ryntric.util.Util;
import sun.misc.Unsafe;

import java.lang.invoke.VarHandle;

/**
 * Off-heap availability buffer used to track which sequences in a ring buffer
 * have been published and are therefore available for consumption.
 * <p>
 * This buffer maintains an integer "availability flag" per slot in the ring.
 * A flag is computed based on the sequence number (shifted by {@code log2(size)})
 * and written into the slot when an event is published. Consumers check the flag
 * to determine whether a sequence is available.
 *
 * <p>
 * Memory is allocated off-heap via {@link sun.misc.Unsafe} for low-level control
 * and to minimize GC pressure. Each slot is aligned using {@code SCALE_FACTOR}
 * and {@code Constants.BYTE_BUFFER_PADDING} to avoid false sharing.
 **/

final class AvailabilityBuffer {
    private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    private static final int SCALE_FACTOR = 2;

    private final long capacity;
    private final long baseAddress;
    private final long shift;
    private final long mask;

    /**
     * Creates a new availability buffer for the given ring size.
     * @param size the ring buffer size (must be a power of 2)
     */
    public AvailabilityBuffer(int size) {
        this.capacity = getCapacity(size);
        this.baseAddress = UNSAFE.allocateMemory(capacity);
        this.mask = size - 1;
        this.shift = Util.log2(size);
        this.init();
    }

    /**
     * Initializes all slots in the buffer to {@code -1} (unavailable).
     */
    private void init() {
        UNSAFE.setMemory(baseAddress, capacity, (byte) -1);
    }

    /**
     * Computes the off-heap memory capacity required for the given size,
     * including scaling and padding.
     */
    private long getCapacity(long size) {
        return (size << SCALE_FACTOR) + (Constants.BYTE_BUFFER_PADDING << 1);
    }

    /**
     * Calculates the availability flag for a given sequence.
     * The flag increments when the sequence wraps around the ring buffer.
     */
    private int calculateAvailabilityFlag(long sequence) {
        return (int) (sequence >>> shift);
    }

    /**
     * Computes the off-heap address for the given sequence.
     */
    private long calculateAddress(long sequence) {
        return (Util.wrapLongIndex(sequence, mask) << SCALE_FACTOR) + baseAddress + Constants.BYTE_BUFFER_PADDING;
    }

    /**
     * Returns the highest contiguous sequence number in the specified range that has been marked as available.
     * To process items without encountering unpublished slots. The method stops at the first sequence whose availability
     * flag does not match the expected value.
     *
     * @param low  the lowest sequence to check (inclusive)
     * @param high the highest sequence to check (inclusive)
     * @return the highest sequence number in the range that is available,
     * or {@code low - 1} if none are available
     */
    public long getAvailable(long low, long high) {
        for (long sequence = low; sequence <= high; sequence++) {
            long address = calculateAddress(sequence);
            int flag = calculateAvailabilityFlag(sequence);
            if (UNSAFE.getIntVolatile(null, address) != flag) {
                return sequence - 1;
            }
        }
        return high;
    }


    /**
     * Marks the given sequence as available by writing its availability flag.
     *
     * @param sequence the sequence number to mark
     */
    public void set(long sequence) {
        long address = calculateAddress(sequence);
        int flag = calculateAvailabilityFlag(sequence);
        UNSAFE.putOrderedInt(null, address, flag);
    }

    /**
     * Marks a contiguous range of sequences as available in the buffer.
     * Each sequence in the range receives its corresponding availability flag.
     *
     * @param low  the lowest sequence number in the range to mark as available
     * @param high the highest sequence number in the range to mark as available (inclusive)
     */
    public void setRange(long low, long high) {
        for (long sequence = low; sequence <= high; sequence++) {
            long address = calculateAddress(sequence);
            int flag = calculateAvailabilityFlag(sequence);
            UNSAFE.putOrderedInt(null, address, flag);
        }
    }

    public void close() {
        UNSAFE.freeMemory(baseAddress);
    }
}
