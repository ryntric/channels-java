package io.github.ryntric;

/**
 * Coordinates the interaction between producers and consumers in a {@link Channel}
 * or {@link RingBuffer}, managing how each side waits when no work is available.
 * <p>
 * The {@code Coordinator} delegates waiting and signaling behavior to the configured
 * {@link ProducerWaitStrategy} and {@link ConsumerWaitStrategy}, allowing fine-grained
 * control over latency and CPU utilization.
 * </p>
 *
 * <p>Typical usage involves invoking {@link #producerWait()} when a producer cannot
 * advance due to capacity constraints, and {@link #consumerWait()} when a consumer
 * has no available data. The {@link #wakeupConsumer()} method signals blocked or
 * parked consumers that new data has arrived.</p>
 *
 * Characteristics
 * <ul>
 *   <li>Thread-safe: multiple producers and consumers can safely share a single coordinator.</li>
 *   <li>Wait strategies are pluggable and configurable per use case.</li>
 *   <li>Supports signaling for blocking consumers and efficient spinning/yielding for others.</li>
 * </ul>
 *
 * @see ProducerWaitStrategy
 * @see ConsumerWaitStrategy
 * @see Channel
 * @see RingBuffer
 * @since 1.0
 */
final class Coordinator {
    /** The wait strategy used by producers when they cannot advance. */
    private final ProducerWaitStrategy producerWaitStrategy;

    /** The wait strategy used by consumers when no items are available. */
    private final ConsumerWaitStrategy consumerWaitStrategy;

    /**
     * Creates a new {@code Coordinator} with the specified producer and consumer wait strategies.
     *
     * @param producerWaitStrategy the strategy used by producers when waiting
     * @param consumerWaitStrategy the strategy used by consumers when waiting
     */
    Coordinator(ProducerWaitStrategy producerWaitStrategy, ConsumerWaitStrategy consumerWaitStrategy) {
        this.producerWaitStrategy = producerWaitStrategy;
        this.consumerWaitStrategy = consumerWaitStrategy;
    }

    /**
     * Causes the producer to wait according to its configured wait strategy.
     */
    public void producerWait() {
        producerWaitStrategy.await();
    }

    /**
     * Causes the consumer to wait according to its configured wait strategy.
     */
    public void consumerWait() {
        consumerWaitStrategy.await();
    }

    /**
     * Signals the consumer that work is available, waking it if blocked or parked.
     */
    public void wakeupConsumer() {
        consumerWaitStrategy.signal();
    }
}
