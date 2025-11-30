package io.github.ryntric;

/**
 * A high-performance {@link ConsumerWaitStrategy} that uses CPU-level spin-waiting
 * via {@link Thread#onSpinWait()} when no data is available.
 * <p>
 * This strategy is designed for low-latency consumer loops where minimal wake-up
 * delay is critical. It repeatedly spins without yielding the CPU or blocking,
 * which maximizes responsiveness at the cost of higher CPU usage.
 * </p>
 *
 * <p>This implementation is <b>non-signallable</b>; the {@link #signal()} method is
 * intentionally a no-op because the consumer thread continuously spins in the
 * polling loop. This strategy is best suited for dedicated threads in tight loops
 * on systems with spare CPU capacity.</p>
 *
 * Characteristics
 * <ul>
 *   <li>Very low latency due to immediate spin-resume.</li>
 *   <li>High CPU usage, especially if the consumer has no work to process.</li>
 *   <li>Does not rely on external signaling — the consumer continuously polls.</li>
 * </ul>
 *
 * @see ConsumerWaitStrategy
 * @see Thread#onSpinWait()
 */
final class ConsumerSpinningStrategy implements ConsumerWaitStrategy {

    /**
     * Performs a single CPU spin-wait cycle.
     * <p>
     * Typically invoked repeatedly in a tight polling loop to minimize latency
     * when waiting for new data to become available.
     * </p>
     */
    @Override
    public void await() {
        Thread.onSpinWait();
    }

    /**
     * No-op for this strategy.
     * <p>
     * Signaling is unnecessary because the consumer thread continuously spins
     * until work is available.
     * </p>
     */
    @Override
    public void signal() {
        // no-op
    }
}
