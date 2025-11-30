package io.github.ryntric;


import java.util.function.Consumer;

/**
 * A {@link Poller} implementation for multi-threaded consumers.
 * <p>
 * This poller safely allows multiple consumer threads to process items concurrently
 * from a {@link RingBuffer}. It maintains a local {@link Sequence} for each consumer
 * to track progress and ensures items are processed in order without missing or
 * duplicating work.
 * </p>
 *
 * <p>Consumers call {@link #poll(Sequencer, RingBuffer, long, java.util.function.Consumer)}
 * to attempt processing up to {@code batchSize} items. If no items are available,
 * it returns {@link PollerState#IDLE}.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Lock-free, low-latency consumption for multi-threaded scenarios.</li>
 *   <li>Tracks and updates sequences atomically using {@link Sequence}.</li>
 *   <li>Handles exceptions in consumer processing gracefully via {@link AbstractPoller#handle}.</li>
 *   <li>Advances gating sequence after successful batch processing to allow producers
 *       to continue publishing.</li>
 * </ul>
 *
 * @param <T> the type of items to be consumed
 * @see Poller
 * @see AbstractPoller
 * @see RingBuffer
 * @see Sequence
 */
final class MultiThreadPoller<T> extends AbstractPoller<T> {

    /** Tracks the last sequence processed by this poller. */
    private final Sequence sequence = new Sequence(Sequence.INITIAL_VALUE);

    /**
     * Polls the ring buffer for up to {@code batchsize} available items and
     * processes them using the provided {@link java.util.function.Consumer}.
     * <p>
     * This method atomically claims a batch of sequences, retrieves items from
     * the ring buffer, and invokes {@link AbstractPoller#handle} for each item.
     * After processing, it advances the gating sequence so producers can continue
     * publishing.
     * </p>
     *
     * @param sequencer  the sequencer managing sequence availability
     * @param ringBuffer the ring buffer containing items to process
     * @param batchsize  the maximum number of items to process in this poll
     * @param consumer   the consumer function to handle each item
     * @return {@link PollerState#PROCESSING} if items were processed,
     *         {@link PollerState#IDLE} if no items were available
     */
    @Override
    public PollerState poll(Sequencer sequencer, RingBuffer<T> ringBuffer, long batchsize, Consumer<T> consumer) {
        long current;
        long next;
        long available;
        long highest;

        do {
            current = sequence.getAcquire();
            next = current + 1;
            available = Long.min(sequencer.getCursorSequenceAcquire(), current + batchsize);

            if (next > available) {
                return PollerState.IDLE;
            }

            highest = sequencer.getHighest(next, available);
        } while (!sequence.weakCompareAndSetVolatile(current, highest));

        for (; next <= highest; next++) {
            handle(consumer, ringBuffer.dequeue(next), next);
        }

        sequencer.advanceGatingSequence(highest, current);
        return PollerState.PROCESSING;
    }

}
