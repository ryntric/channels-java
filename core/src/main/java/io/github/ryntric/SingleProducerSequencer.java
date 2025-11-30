package io.github.ryntric;

abstract class SingleProducerSequencerLeftPaddings extends AbstractSequencer {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public SingleProducerSequencerLeftPaddings(int bufferSize) {
        super(bufferSize);
    }
}

abstract class SingleProducerSequencerFields extends SingleProducerSequencerLeftPaddings {
    /** The last claimed sequence for the producer. */
    long sequence = Sequence.INITIAL_VALUE;

    /** Cached value of the last observed gating sequence. */
    long cached = Sequence.INITIAL_VALUE;

    public SingleProducerSequencerFields(int bufferSize) {
        super(bufferSize);
    }
}

abstract class SingleProducerSequencerRightPaddings extends SingleProducerSequencerFields {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public SingleProducerSequencerRightPaddings(int bufferSize) {
        super(bufferSize);
    }
}

/**
 * A high-performance sequencer for a single producer writing to a ring buffer.
 * <p>
 * This class provides single-producer semantics using a cached gating sequence
 * and cursor sequence. Producers claim sequences via {@link #next(Coordinator, int)}
 * and publish them via {@link #publishCursorSequence(long)} or
 * {@link #publishCursorSequence(long, long)}.
 * </p>
 *
 * <p>Because this is a single-producer sequencer, it does not require an
 * {@link AvailabilityBuffer} or complex CAS operations for multi-threaded producers.
 * </p>
 *
 * @see AbstractSequencer
 * @see Sequencer
 */
final class SingleProducerSequencer extends SingleProducerSequencerRightPaddings implements Sequencer {

    SingleProducerSequencer(int bufferSize) {
        super(bufferSize);
    }

    /**
     * @see Sequencer
     */
    @Override
    public long next(Coordinator coordinator, int n) {
        long cached = this.cached;
        long next = sequence + n;
        long wrapPoint = next - bufferSize;

        if (wrapPoint > cached) {
            this.cached = wait(coordinator, gatingSequence, wrapPoint);
        }

        this.sequence = next;
        return next;
    }

    /**
     * @see Sequencer
     */
    @Override
    public void publishCursorSequence(long value) {
        cursorSequence.setRelease(value);
    }

    /**
     * @see Sequencer
     */
    @Override
    public void publishCursorSequence(long low, long high) {
        cursorSequence.setRelease(high);
    }

    /**
     * @see Sequencer
     */
    @Override
    public long getHighest(long low, long high) {
        return high;
    }

    /**
     * @see Sequencer#close()
     */
    @Override
    public void close() {
        // no-op
    }
}
