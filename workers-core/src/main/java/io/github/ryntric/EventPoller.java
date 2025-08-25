package io.github.ryntric;

import io.github.ryntric.util.Util;

/**
 * author: ryntric
 * date: 8/25/25
 * time: 12:13 PM
 **/

public final class EventPoller<T> {
    private final RingBuffer<T> buffer;
    private final Sequencer sequencer;
    private final Sequence sequence;
    private final Sequence gatingSequence;
    private final int batchSize;

    public EventPoller(RingBuffer<T> buffer, BatchSizeLimit batchSizeLimit) {
        this.buffer = buffer;
        this.sequencer = buffer.getSequencer();
        this.sequence = sequencer.getGatingSequence();
        this.gatingSequence = sequencer.getCursorSequence();
        this.batchSize = Util.assertBatchSizeGreaterThanZero(batchSizeLimit.get(buffer.size()));
    }

    private long getHighest(long current, long next, long available) {
        return Long.min(current + batchSize, sequencer.getHighestPublishedSequence(next, available));
    }

    private void handle(EventHandler<T> handler, T event, long sequence) {
        try {
            handler.onEvent(event, sequence);
        } catch (Throwable ex) {
            handler.onError(ex);
        }
    }

    public PollState poll(EventHandler<T> handler) {
        Sequence gatingSequence = this.gatingSequence;
        Sequence sequence = this.sequence;
        RingBuffer<T> buffer = this.buffer;

        long current = sequence.getPlain();
        long next = current + 1;
        long available;

        if ((available = gatingSequence.getPlain()) < next && (available = gatingSequence.getAcquire()) < next) {
            return PollState.IDLE;
        }

        long highest = getHighest(current, next, available);
        while (next <= highest) {
            handle(handler, buffer.get(next), next);
            next++;
        }

        sequence.setRelease(next - 1);
        return PollState.PROCESSING;

    }
}
