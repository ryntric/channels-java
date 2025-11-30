package io.github.ryntric;


/**
 * A {@link ProducerWaitStrategy} that yields the CPU when the producer cannot advance.
 * <p>
 * By invoking {@link Thread#yield()}, the producer thread hints to the scheduler
 * that it is willing to yield its current time slice, allowing other threads to run.
 * This provides a middle ground between spinning and parking, balancing latency
 * and CPU usage.
 * </p>
 *
 * Characteristics
 * <ul>
 *   <li>Lower CPU usage than spinning, but slightly higher latency.</li>
 *   <li>Does not block the thread.</li>
 *   <li>Does not rely on signaling — the producer continuously polls.</li>
 * </ul>
 *
 * @see ProducerWaitStrategy
 * @see Thread#yield()
 * @since 1.0
 */
final class ProducerYieldingStrategy implements ProducerWaitStrategy {

    /**
     * Yields the CPU to allow other threads to run.
     * <p>
     * Typically invoked repeatedly in a producer loop when the buffer has no free slots.
     * </p>
     */
    @Override
    public void await() {
        Thread.yield();
    }
}
