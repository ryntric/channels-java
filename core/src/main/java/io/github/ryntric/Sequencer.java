package io.github.ryntric;


interface Sequencer {

    /**
     * Claims the next available sequence in the buffer for publishing a single item.
     * <p>
     * This is a convenience method that delegates to {@link #next(Coordinator, int)}
     * with {@code n = 1}. It requests one available slot from the sequencer while
     * respecting backpressure and coordination rules defined by the {@link Coordinator}.
     * </p>
     *
     * @param coordinator the coordinator responsible for managing producer wait strategy
     * @return the next available sequence for publishing
     */
    default long next(Coordinator coordinator) {
        return next(coordinator, 1);
    }

    /**
     * Claims the next {@code n} available sequences in the buffer for publishing.
     * <p>
     * This method is used by producers to reserve one or more slots in
     * a ring buffer. It blocks or waits, depending on the {@link Coordinator}
     * strategy, until sufficient capacity is available to claim the requested range.
     * </p>
     *
     * @param coordinator the coordinator that applies the chosen wait strategy (spin, yield, park, etc.)
     * @param n           the number of sequences to claim (must be positive)
     * @return the highest claimed sequence number in the reserved range
     */
    long next(Coordinator coordinator, int n);

    /**
     * Publishes a single sequence, making the corresponding item visible
     * to consumers.
     * <p>
     * This should be called after the producer has finished writing the
     * item associated with the given {@code sequence}. Publication uses
     * release semantics to ensure correct memory visibility for consumers.
     * </p>
     *
     * @param sequence the sequence to publish
     */
    void publishCursorSequence(long sequence);

    /**
     * Publishes a contiguous range of sequences, marking all items in the
     * specified range as available to consumers.
     * <p>
     * This method is used after a producer batch-fills multiple
     * slots in the buffer. It ensures that the full range from {@code low}
     * to {@code high} (inclusive) becomes visible in order.
     * </p>
     *
     * @param low  the first sequence in the range to publish (inclusive)
     * @param high the last sequence in the range to publish (inclusive)
     */
    void publishCursorSequence(long low, long high);

    /**
     * Publishes the given sequence to the gating sequence using release semantics.
     * This method is typically invoked by a consumer to indicate that it has
     * completed processing up to a specific sequence.
     *
     * @param sequence the sequence to publish
     */
    void publishGatingSequence(long sequence);

    /**
     * Advances the gating sequence up to the specified target sequence,
     * retrying in a weak compare-and-set loop until successful.
     *
     * <p>This ensures that concurrent updates do not regress the gating
     * sequence and maintains monotonic progression.</p>
     *
     * @param sequence the target sequence to advance to
     * @param current  the expected current sequence value
     */
    void advanceGatingSequence(long sequence, long current);

    /**
     * Returns the highest available sequence number within the given range.
     * <p>
     * This method determines the maximum sequence value that can be safely
     * processed between the specified {@code low} (inclusive)
     * and {@code high} (inclusive) sequence boundaries.
     * </p>
     *
     * @param low  the lower bound of the sequence range (inclusive)
     * @param high the upper bound of the sequence range (inclusive)
     * @return the highest sequence within the range that is currently available
     */
    long getHighest(long low, long high);

    /**
     * Returns the current cursor sequence using acquire semantics.
     *
     * <p>This method provides visibility guarantees for other threads reading
     * the current cursor position.</p>
     *
     * @return the current cursor sequence value
     */
    long getCursorSequenceAcquire();

    /**
     * Returns the current gating sequence using acquire semantics.
     *
     * <p>This method provides visibility guarantees for other threads reading
     * the current gating position.</p>
     *
     * @return the current gating sequence value
     */
    long getGatingSequenceAcquire();

    /**
     * Returns the current gating sequence using plain (non-volatile) semantics.
     *
     * @return the current gating sequence value
     */
    long getGatingSequencePlain();

    /**
     * Waits for the gating sequence to advance past the specified wrap point.
     *
     * <p>This is used by producers to avoid overwriting unconsumed data in
     * a ring buffer. The waiting strategy is delegated to the provided
     * {@link Coordinator}, which may use spinning, yielding, or parking.</p>
     *
     * @param coordinator    the coordinator responsible for handling wait strategy
     * @param gatingSequence the sequence to monitor
     * @param wrapPoint      the minimum required gating sequence
     * @return the latest gating sequence value once it has advanced past the wrap point
     */
    long wait(Coordinator coordinator, Sequence gatingSequence, long wrapPoint);

    /**
     * Releases any resources held by this sequencer and performs cleanup.
     * Implementations use this method to free off-heap memory.
     */
    void close();

}
