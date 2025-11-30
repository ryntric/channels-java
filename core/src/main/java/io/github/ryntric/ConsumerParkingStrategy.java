package io.github.ryntric;


import java.util.concurrent.locks.LockSupport;

/**
 * A low-power {@link ConsumerWaitStrategy} that uses {@link LockSupport#parkNanos(long)}
 * to briefly park the consumer thread when no data is available.
 * <p>
 * This strategy is useful when balancing power efficiency and latency is important.
 * The consumer thread yields control to the operating system for a very short time
 * (1 nanosecond) rather than busy-spinning, reducing CPU load while maintaining
 * relatively fast wake-up times.
 * </p>
 *
 * <p>This implementation is <b>non-signallable</b>; the {@link #signal()} method is
 * intentionally a no-op because the parked thread will resume automatically on its
 * next iteration. It is most effective when used in continuous polling loops.</p>
 *
 * Characteristics
 * <ul>
 *   <li>Low CPU usage compared to spinning or yielding.</li>
 *   <li>Suitable for continuously running consumer threads.</li>
 *   <li>Does not rely on external signaling — park duration controls backoff.</li>
 * </ul>
 *
 * @see ConsumerWaitStrategy
 * @see LockSupport#parkNanos(long)
 */
final class ConsumerParkingStrategy implements ConsumerWaitStrategy {

    /**
     * Parks the consumer thread for a short, fixed duration (1 nanosecond).
     * <p>
     * This method is invoked repeatedly in a polling loop, allowing
     * the thread to yield execution momentarily between checks for new data.
     * </p>
     */
    @Override
    public void await() {
        LockSupport.parkNanos(1L);
    }

    /**
     * No-op for this strategy.
     * <p>
     * The consumer thread resumes automatically after the park duration,
     * so explicit signaling is not required.
     * </p>
     */
    @Override
    public void signal() {
        // no-op
    }
}
