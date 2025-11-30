package io.github.ryntric;

/**
 * Defines a strategy for managing how a consumer thread waits when no data is
 * immediately available in a {@link Channel} or {@link RingBuffer}.
 * <p>
 * Implementations control the backoff behavior of consumers, balancing CPU usage
 * and latency according to the needs of the system. Common strategies include:
 * spinning, yielding, parking, or blocking.
 * </p>
 *
 * <p>Typical usage involves invoking {@link #await()} in a consumer polling loop
 * when no work is available, and calling {@link #signal()} to notify the consumer
 * that new data has arrived (if the strategy supports signaling).</p>
 *
 * Implementation Notes
 * <ul>
 *   <li>Spin-based strategies usually ignore {@link #signal()}.</li>
 *   <li>Blocking strategies typically require {@link #signal()} to wake the thread.</li>
 *   <li>Implementations should be thread-safe.</li>
 * </ul>
 *
 * @see ConsumerSpinningStrategy
 * @see ConsumerYieldingStrategy
 * @see ConsumerParkingStrategy
 * @see ConsumerBlockingStrategy
 * @since 1.0
 */
interface ConsumerWaitStrategy {

    /**
     * Invoked when the consumer should wait for work.
     * <p>
     * The behavior depends on the specific strategy:
     * spinning, yielding, parking, or blocking.
     * </p>
     */
    void await();


    /**
     * Invoked to signal the consumer that work is available.
     * <p>
     * Depending on the implementation, this method may be a no-op
     * (for spinning or yielding strategies) or required to wake a blocked
     * consumer thread (for blocking strategies).
     * </p>
     */
    void signal();
}
