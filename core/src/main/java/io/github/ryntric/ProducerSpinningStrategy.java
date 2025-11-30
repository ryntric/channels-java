package io.github.ryntric;

/**
 * A high-performance {@link ProducerWaitStrategy} that uses CPU-level spin-waiting
 * via {@link Thread#onSpinWait()} when the producer cannot advance.
 * <p>
 * This strategy is designed for low-latency scenarios where minimal wake-up
 * delay is critical. The producer continuously spins without yielding or blocking,
 * maximizing responsiveness at the cost of higher CPU usage.
 * </p>
 *
 * Characteristics
 * <ul>
 *   <li>Very low latency due to immediate spin-resume.</li>
 *   <li>High CPU usage if the buffer has no available capacity.</li>
 *   <li>Does not rely on external signaling — the producer continuously polls.</li>
 * </ul>
 *
 * @see ProducerWaitStrategy
 * @see Thread#onSpinWait()
 */
final class ProducerSpinningStrategy implements ProducerWaitStrategy {

    /**
     * Performs a single CPU spin-wait cycle.
     * <p>
     * Invoked repeatedly in a tight producer loop when the buffer is full.
     * </p>
     */
    @Override
    public void await() {
        Thread.onSpinWait();
    }
}
