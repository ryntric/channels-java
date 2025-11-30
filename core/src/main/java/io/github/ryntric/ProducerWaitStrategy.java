package io.github.ryntric;

/**
 * Defines a strategy for how a producer thread waits when it cannot advance
 * in a {@link RingBuffer} due to lack of available capacity.
 * <p>
 * Implementations control the backoff behavior of producers, balancing CPU usage
 * and latency according to the needs of the system. Common strategies include:
 * spinning, yielding, or parking.
 * </p>
 *
 * <p>Typical usage involves calling {@link #await()} in a producer loop when
 * the buffer has no free slots. The specific wait behavior depends on the
 * implementation.</p>
 *
 * Implementation Notes
 * <ul>
 *   <li>Spin-based strategies continuously poll the buffer and do not yield the CPU.</li>
 *   <li>Yielding strategies hint the scheduler to allow other threads to run.</li>
 *   <li>Parking strategies suspend the thread briefly to reduce CPU usage.</li>
 * </ul>
 *
 * @see ProducerSpinningStrategy
 * @see ProducerYieldingStrategy
 * @see ProducerParkingStrategy
 */
interface ProducerWaitStrategy {

    /**
     * Causes the producer thread to wait according to the configured strategy.
     * <p>
     * This method is invoked when the buffer has no available capacity for the
     * producer to write to. The behavior depends on the implementation (spin,
     * yield, or park).
     * </p>
     */
    void await();
}
