package io.github.ryntric;


import java.util.function.Consumer;

final class SingleThreadPoller<T> extends AbstractPoller<T> {

    @Override
    public PollerState poll(Sequencer sequencer, RingBuffer<T> ringBuffer, long batchSize, Consumer<T> consumer) {
        long current = sequencer.getGatingSequencePlain();
        long next = current + 1;
        long available = Long.min(sequencer.getCursorSequenceAcquire(), current + batchSize);

        if (next > available) {
            return PollerState.IDLE;
        }

        long highest = sequencer.getHighest(next, available);
        for (; next <= highest; next++) {
            handle(consumer, ringBuffer.dequeue(next), next);
        }
        sequencer.publishGatingSequence(highest);
        return PollerState.PROCESSING;
    }
}
