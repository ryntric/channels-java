package io.github.ryntric;

import io.github.ryntric.util.Util;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
final class RingBuffer<T> {
    private final T[] buffer;
    private final int mask;
    private final Sequencer sequencer;
    private final Poller<T> poller;

    RingBuffer(Sequencer sequencer, Poller<T> poller, int size) {
        Util.assertThatPowerOfTwo(size);
        this.mask = size - 1;
        this.sequencer = sequencer;
        this.poller = poller;
        this.buffer = (T[]) new Object[(Constants.OBJECT_ARRAY_PADDING << 1) + size];
    }

    private int wrapIndex(long sequence) {
        return Util.wrapIndex(sequence, mask) + Constants.OBJECT_ARRAY_PADDING;
    }

    T dequeue(long sequence) {
        int index = wrapIndex(sequence);
        T value = buffer[index];
        buffer[index] = null;
        return value;
    }

    public PollerState poll(int batchsize, Consumer<T> consumer) {
        return poller.poll(sequencer, this, batchsize, consumer);
    }

    public void push(Coordinator coordinator, T item) {
        long sequence = sequencer.next(coordinator);
        buffer[wrapIndex(sequence)] = item;
        sequencer.publishCursorSequence(sequence);
    }

    public void push(Coordinator coordinator, T[] items) {
        int length = items.length;
        long high = sequencer.next(coordinator, length);
        long low = high - (length - 1);

        for (int i = 0; i < length; i++) {
            buffer[wrapIndex(low + i)] = items[i];
        }

        sequencer.publishCursorSequence(low, high);
    }

    public void push(Coordinator coordinator, Collection<T> items) {
        int size = items.size();
        long high = sequencer.next(coordinator, size);
        long low = high - (size - 1);
        Iterator<T> iterator = items.iterator();

        for (int i = 0; iterator.hasNext(); i++) {
            buffer[wrapIndex(low + i)] = iterator.next();
        }

        sequencer.publishCursorSequence(low, high);
    }

    public long size() {
        return sequencer.getCursorSequenceAcquire() - sequencer.getGatingSequenceAcquire();
    }

    public void close() {
        sequencer.close();
    }

}
