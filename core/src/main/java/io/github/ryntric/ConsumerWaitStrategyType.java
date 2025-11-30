package io.github.ryntric;


/**
 * Enumerates the available wait strategies for consumers in a {@link Channel} or
 * {@link RingBuffer}.
 * <p>
 * This type is used to configure the behavior of a consumer thread when it cannot
 * advance due to lack of available items in the buffer.
 * </p>
 *
 * <ul>
 *   <li>{@link #SPINNING} – The consumer continuously spins, minimizing latency at the cost of higher CPU usage.</li>
 *   <li>{@link #PARKING} – The consumer briefly parks the thread using {@link java.util.concurrent.locks.LockSupport}, reducing CPU usage.</li>
 *   <li>{@link #YIELDING} – The consumer yields the CPU to allow other threads to run, balancing latency and CPU usage.</li>
 *   <li>{@link #BLOCKING} – The consumer blocks and waits for a notification to resume, minimizing CPU usage but adding higher latency.</li>
 * </ul>
 *
 * @see ConsumerWaitStrategy
 * @see Channel
 */
public enum ConsumerWaitStrategyType {
    /** Continuous spin-wait for low-latency consumer loops. */
    SPINNING,

    /** Briefly parks the consumer thread to reduce CPU usage. */
    PARKING,

    /** Yields the CPU to other threads, balancing latency and CPU usage. */
    YIELDING,

    /** Blocks the consumer thread until notified, minimizing CPU usage. */
    BLOCKING
}
