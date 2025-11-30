package io.github.ryntric;


/**
 * A blocking {@link ConsumerWaitStrategy} implementation that uses a
 * {@link Object#wait()} / {@link Object#notifyAll()} mechanism to suspend and
 * resume the consumer thread.
 * <p>
 * This strategy is ideal for low-throughput or background consumers that do not
 * require busy-waiting or spinning, reducing CPU usage at the cost of higher
 * latency on wake-up.
 * </p>
 *
 * <p>The consumer thread will block inside {@link #await()} until another thread
 * (typically the producer or coordinator) invokes {@link #signal()}, which
 * releases the waiting consumer.</p>
 *
 * Thread Safety
 * <ul>
 *   <li>All blocking and signaling is synchronized on a shared mutex.</li>
 *   <li>Multiple consumers can safely use separate instances of this strategy.</li>
 *   <li>This strategy is not designed for high-frequency wakeups.</li>
 * </ul>
 *
 * @see ConsumerWaitStrategy
 * @see java.lang.Object#wait()
 * @see java.lang.Object#notifyAll()
 */
final class ConsumerBlockingStrategy implements ConsumerWaitStrategy {
    /** The monitor object used for synchronization between await and signal calls. */
    private final Object MUTEX = new Object();

    /** A flag indicating whether the consumer is currently blocked. */
    private boolean isBlocked = true;

    /**
     * Blocks the calling thread until {@link #signal()} is invoked.
     * <p>
     * The method waits on the internal mutex while {@code isBlocked} is {@code true}.
     * Once signaled, it resets {@code isBlocked} to {@code true} to prepare for
     * the next blocking cycle.
     * </p>
     *
     * <p>If the thread is interrupted while waiting, the interruption is ignored.</p>
     */
    @Override
    public void await() {
        try {
            synchronized (MUTEX) {
                while (isBlocked) {
                    MUTEX.wait();
                }
                isBlocked = true;

            }
        } catch (InterruptedException ignored) {
            // no-op
        }

    }

    /**
     * Wakes up any thread currently blocked in {@link #await()}.
     * <p>
     * This method clears the {@code isBlocked} flag and notifies all threads
     * waiting on the mutex. Typically called by the producer or coordinator
     * after publishing new data.
     * </p>
     */
    @Override
    public void signal() {
        synchronized (MUTEX) {
            isBlocked = false;
            MUTEX.notifyAll();
        }
    }
}
