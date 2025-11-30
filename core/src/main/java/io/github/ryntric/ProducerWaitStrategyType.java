package io.github.ryntric;

/**
 * Enumerates the available wait strategies for producers in a {@link Channel} or
 * {@link RingBuffer}.
 * <p>
 * This type is used to configure the behavior of a producer thread when it cannot
 * advance due to lack of available capacity.
 * </p>
 *
 * <ul>
 *   <li>{@link #SPINNING} – The producer continuously spins, minimizing latency at the cost of higher CPU usage.</li>
 *   <li>{@link #PARKING} – The producer briefly parks the thread using {@link java.util.concurrent.locks.LockSupport}, reducing CPU usage.</li>
 *   <li>{@link #YIELDING} – The producer yields the CPU to allow other threads to run, providing a balance between latency and CPU usage.</li>
 * </ul>
 *
 * @see ProducerWaitStrategy
 * @see Channel
 */
public enum ProducerWaitStrategyType {
    /** Continuous spin-wait for low-latency producer loops. */
    SPINNING,

    /** Briefly parks the producer thread to reduce CPU usage. */
    PARKING,

    /** Yields the CPU to other threads, balancing latency and CPU usage. */
    YIELDING
}
