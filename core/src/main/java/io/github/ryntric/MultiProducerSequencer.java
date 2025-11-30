package io.github.ryntric;

abstract class MultiProducerSequencerLeftPaddings extends AbstractSequencer {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public MultiProducerSequencerLeftPaddings(int bufferSize) {
        super(bufferSize);
    }
}

abstract class MultiProducerSequencerFields extends MultiProducerSequencerLeftPaddings {
    protected long cached = Sequence.INITIAL_VALUE;

    public MultiProducerSequencerFields(int bufferSize) {
        super(bufferSize);
    }
}

abstract class MultiProducerSequencerRightPaddings extends MultiProducerSequencerFields {
    protected byte
            p10, p11, p12, p13, p14, p15, p16, p17,
            p20, p21, p22, p23, p24, p25, p26, p27,
            p30, p31, p32, p33, p34, p35, p36, p37,
            p40, p41, p42, p43, p44, p45, p46, p47,
            p50, p51, p52, p53, p54, p55, p56, p57,
            p60, p61, p62, p63, p64, p65, p66, p67,
            p70, p71, p72, p73, p74, p75, p76, p77;

    public MultiProducerSequencerRightPaddings(int bufferSize) {
        super(bufferSize);
    }
}

/**
 * A high-performance sequencer for multiple producers writing concurrently
 * to a ring buffer.
 * <p>
 * This class extends {@link AbstractSequencer} with multi-producer semantics,
 * using an {@link AvailabilityBuffer} to track published sequences and
 * a cached gating sequence to reduce volatile reads.
 * </p>
 *
 * <p>Producers claim sequences using {@link #next(Coordinator, int)} and
 * publish them using {@link #publishCursorSequence(long)} or
 * {@link #publishCursorSequence(long, long)}. Consumers can query the
 * highest available sequence with {@link #getHighest(long, long)}.</p>
 *
 * <p>The left and right padding classes separate critical fields onto separate
 * cache lines, minimizing false sharing under concurrent updates.</p>
 *
 * @see AvailabilityBuffer
 * @see AbstractSequencer
 */
final class MultiProducerSequencer extends MultiProducerSequencerRightPaddings {
    private final AvailabilityBuffer availabilityBuffer;

    public MultiProducerSequencer(int bufferSize) {
        super(bufferSize);
        this.availabilityBuffer = new AvailabilityBuffer(bufferSize);
    }

    @Override
    public long next(Coordinator coordinator, int n) {
        long cached = this.cached;
        long next = cursorSequence.getAndAddVolatile(n) + n;
        long wrapPoint = next - bufferSize;

        if (wrapPoint > cached) {
            this.cached = wait(coordinator, gatingSequence, wrapPoint);
        }

        return next;
    }

    /**
     * @see Sequencer#publishCursorSequence(long sequence)
     */
    @Override
    public void publishCursorSequence(long sequence) {
        availabilityBuffer.set(sequence);
    }

    /**
     * @see Sequencer#publishCursorSequence(long low, long high)
     */
    @Override
    public void publishCursorSequence(long low, long high) {
        availabilityBuffer.setRange(low, high);
    }

    /**
     * @see Sequencer#getHighest(long next, long available)
     */
    @Override
    public long getHighest(long next, long available) {
        return availabilityBuffer.getAvailable(next, available);
    }

    /**
     * @see Sequencer#close()
     */
    @Override
    public void close() {
        availabilityBuffer.close();
    }
}
