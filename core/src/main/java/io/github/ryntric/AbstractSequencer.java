package io.github.ryntric;

/**
 * An abstract base implementation of the {@link Sequencer} interface providing
 * common functionality for sequence coordination in a ring buffer or similar
 * high-throughput data structure.
 *
 * <p>This class manages two key sequence counters:</p>
 * <ul>
 *   <li>{@link #cursorSequence} – tracks the latest published sequence.</li>
 *   <li>{@link #gatingSequence} – tracks the minimum sequence of consumers.</li>
 * </ul>
 *
 * <p>Implementations are responsible for defining how sequences are claimed,
 * published, and coordinated between producers and consumers. This base class
 * provides thread-safe primitives for advancing and publishing gating sequences,
 * as well as waiting for sequence availability.</p>
 *
 * <p>All sequence updates are performed using release/acquire semantics to ensure
 * correct visibility and ordering in concurrent environments.</p>
 */
abstract class AbstractSequencer implements Sequencer {
    /** The initial value of a sequence before any events are published. */
    public static final long INITIAL_CURSOR_VALUE = -1L;

    /** The size of the underlying ring buffer. */
    protected final int bufferSize;

    /** The main sequence representing the latest published position. */
    protected final Sequence cursorSequence;

    /** The sequence tracking the progress of consumers (the gating sequence). */
    protected final Sequence gatingSequence;

    /**
     * Creates a new {@code AbstractSequencer} with the specified buffer size.
     *
     * @param bufferSize the size of the buffer that this sequencer coordinates
     */
    public AbstractSequencer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.cursorSequence = new Sequence(INITIAL_CURSOR_VALUE);
        this.gatingSequence = new Sequence(INITIAL_CURSOR_VALUE);
    }


    @Override
    public final void publishGatingSequence(long sequence) {
        gatingSequence.setRelease(sequence);
    }


    @Override
    public final void advanceGatingSequence(long sequence, long current) {
        Sequence gatingSequence = this.gatingSequence;

        while (current < sequence && !gatingSequence.weakCompareAndSetVolatile(current, sequence)) {
            current = gatingSequence.getAcquire();
        }
    }

    @Override
    public final long getCursorSequenceAcquire() {
        return cursorSequence.getAcquire();
    }

    @Override
    public long getGatingSequenceAcquire() {
        return gatingSequence.getAcquire();
    }

    @Override
    public final long getGatingSequencePlain() {
        return gatingSequence.getPlain();
    }

    @Override
    public final long wait(Coordinator coordinator, Sequence gatingSequence, long wrapPoint) {
        long gating;
        while (wrapPoint > (gating = gatingSequence.getAcquire())) {
            coordinator.producerWait();
        }
        return gating;
    }

}
