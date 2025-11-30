package io.github.ryntric;


import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract base implementation of the {@link Poller} interface that provides
 * common functionality for safely handling consumed items.
 * <p>
 * This class centralizes error handling for item processing by wrapping calls to
 * a provided {@link Consumer} within a try-catch block. Any exceptions thrown
 * during item consumption are caught and logged at the {@link Level#WARNING} level.
 * </p>
 *
 * <p>Subclasses can use {@link #handle(Consumer, Object, long)} to process items safely
 * without having to manage exception handling logic themselves.</p>
 *
 * @param <T> the type of item being polled and processed
 *
 * @see Poller
 */
abstract class AbstractPoller<T> implements Poller<T> {
    private static final Logger LOGGER = Logger.getLogger(AbstractPoller.class.getName());

    /**
     * Creates a formatted error message for logging when item processing fails.
     *
     * @param item     the item being processed when the error occurred
     * @param sequence the sequence number of the item in the buffer or stream
     * @return a descriptive error message containing the item and sequence number
     */
    private String createErrorMessage(T item, long sequence) {
        return "Error while processing item with sequence  " + sequence + ", item " + item;
    }

    /**
     * Processes an item using the provided {@link Consumer}, handling any exceptions
     * that may occur during execution.
     * <p>
     * If an exception is thrown while consuming the item, it is caught and logged
     * as a warning using the configured {@link Logger}.
     * </p>
     *
     * @param consumer the consumer responsible for processing the item
     * @param item     the item to process
     * @param sequence the sequence number associated with the item
     */
    protected final void handle(Consumer<T> consumer, T item, long sequence) {
        try {
            consumer.accept(item);
        } catch (Throwable ex) {
            LOGGER.log(Level.WARNING, createErrorMessage(item, sequence), ex);
        }
    }
}
