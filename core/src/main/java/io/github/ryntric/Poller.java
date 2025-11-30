package io.github.ryntric;

import java.util.function.Consumer;


/**
 * Defines a strategy for consuming items from a {@link RingBuffer}.
 * <p>
 * A {@code Poller} is responsible for safely retrieving available items from
 * the ring buffer and passing them to a {@link Consumer} for processing.
 * Implementations may vary depending on single-threaded or multi-threaded
 * consumer scenarios.
 * </p>
 *
 * <p>Typical usage involves repeatedly calling {@link #poll(Sequencer, RingBuffer, long, Consumer)}
 * in a consumer loop. The poller ensures correct sequencing, batch processing,
 * and thread-safe access to the buffer.</p>
 *
 * @param <T> the type of items stored in the ring buffer
 * @see RingBuffer
 * @see Sequencer
 * @see MultiThreadPoller
 * @see SingleThreadPoller
 */
interface Poller<T> {

    /**
     * Attempts to poll up to {@code batchsize} items from the {@link RingBuffer} and
     * process them using the provided {@link Consumer}.
     *
     * @param sequencer  the sequencer that manages sequence availability
     * @param ringBuffer the ring buffer containing items to process
     * @param batchsize  the maximum number of items to process in this poll
     * @param consumer   the consumer function to handle each item
     * @return {@link PollerState#PROCESSING} if one or more items were processed,
     *         {@link PollerState#IDLE} if no items were available
     */
    PollerState poll(Sequencer sequencer, RingBuffer<T> ringBuffer, long batchsize, Consumer<T> consumer);

}
