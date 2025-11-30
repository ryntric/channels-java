package io.github.ryntric;


import java.util.concurrent.locks.LockSupport;

/**
 * A {@link ProducerWaitStrategy} that briefly parks the producer thread using
 * {@link LockSupport#parkNanos(long)} when it cannot advance.
 * <p>
 * This strategy is useful for low-power, low-latency producer loops. The thread
 * yields execution for a very short duration (1 nanosecond) instead of busy-spinning,
 * reducing CPU usage while maintaining relatively fast wake-up times.
 * </p>
 *
 * Characteristics
 * <ul>
 *   <li>Reduces CPU usage compared to spinning or yielding.</li>
 *   <li>Does not rely on signaling; the thread resumes automatically after the park duration.</li>
 *   <li>Ideal for continuously running producer threads where low latency is desired.</li>
 * </ul>
 *
 * @see ProducerWaitStrategy
 * @see LockSupport#parkNanos(long)
 */
final class ProducerParkingStrategy implements ProducerWaitStrategy {

    /**
     * Parks the producer thread for a short, fixed duration (1 nanosecond).
     * <p>
     * Typically invoked repeatedly in a producer loop when the buffer has no available capacity.
     * </p>
     */
    @Override
    public void await() {
        LockSupport.parkNanos(1L);
    }
}
