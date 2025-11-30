package io.github.ryntric;

/**
 * A {@link ConsumerWaitStrategy} that yields the CPU when no data is available.
 * <p>
 * This strategy is a middle ground between spinning and blocking. By invoking
 * {@link Thread#yield()}, the consumer thread hints to the scheduler that it is
 * willing to yield its current time slice, allowing other threads to run.
 * </p>
 *
 * <p>This implementation is <b>non-signallable</b>; the {@link #signal()} method
 * is intentionally a no-op. The consumer thread resumes automatically in the next
 * iteration of its polling loop.</p>
 *
 * Characteristics
 * <ul>
 *   <li>Lower CPU usage than spinning, but slightly higher latency.</li>
 *   <li>Does not block the thread.</li>
 *   <li>Does not rely on signaling — the consumer continuously polls.</li>
 * </ul>
 *
 * @see ConsumerWaitStrategy
 * @see Thread#yield()
 * @since 1.0
 */
final class ConsumerYieldingStrategy implements ConsumerWaitStrategy {

    /**
     * Yields the CPU to allow other threads to run.
     * <p>
     * Called repeatedly in a polling loop when no work is available.
     * </p>
     */
    @Override
    public void await() {
        Thread.yield();
    }

    /**
     * No-op for this strategy.
     * <p>
     * Signaling is unnecessary because the consumer thread continuously polls.
     * </p>
     */
    @Override
    public void signal() {
        //  no-op
    }
}
