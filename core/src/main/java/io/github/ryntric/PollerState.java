package io.github.ryntric;

/**
 * Represents the state of a consumer poll operation in a {@link Poller}.
 * <p>
 * {@code PollState} indicates whether a polling attempt processed items
 * from the {@link RingBuffer} or if no items were available.
 * </p>
 *
 * <ul>
 *   <li>{@link #PROCESSING} – One or more items were successfully consumed.</li>
 *   <li>{@link #IDLE} – No items were available for consumption during this poll.</li>
 * </ul>
 *
 * @see Poller
 * @see RingBuffer
 * @since 1.0
 */

enum PollerState {
    /** Indicates that items were processed during this poll. */
    PROCESSING,
    /** Indicates that no items were available to process during this poll. */
    IDLE
}
