package io.github.ryntric;


import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * A high-performance, lock-free message channel built on top of a {@link RingBuffer}.
 * <p>
 * The {@code Channel} class provides an abstraction for exchanging data between
 * producers and consumers with minimal latency. It supports multiple concurrency
 * configurations such as SPSC (single-producer/single-consumer), MPSC (multi-producer/
 * single-consumer), SPMC (single-producer/multi-consumer), and MPMC (multi-producer/
 * multi-consumer) through specialized {@link Sequencer} and {@link Poller}
 * implementations.
 * </p>
 *
 * <p>Producers publish items using {@link #push(Object)} or {@link #push(Object[])}.
 * Consumers retrieve items using {@link #receive(int, Consumer)} or
 * {@link #blockingReceive(int, Consumer)}.
 * The {@link Coordinator} controls the wait strategies for both sides, allowing
 * fine-grained tuning of CPU utilization and latency characteristics.</p>
 *
 * Usage Example
 * <pre>{@code
 * Channel<String> channel = Channel.mpsc(
 *     1024,
 *     ProducerWaitStrategyType.SPINNING,
 *     ConsumerWaitStrategyType.BLOCKING
 * );
 *
 * // Producer
 * channel.push("Hello");
 *
 * // Consumer
 * channel.blockingReceive(64, msg -> System.out.println("Received: " + msg));
 * }</pre>
 *
 * @param <T> the type of element stored in this channel
 *
 * @see RingBuffer
 * @see Sequencer
 * @see Poller
 * @see Coordinator
 */
public final class Channel<T> {
    private final Coordinator coordinator;
    private final RingBuffer<T> ringBuffer;

    private Channel(Coordinator coordinator, RingBuffer<T> ringBuffer) {
        this.coordinator = coordinator;
        this.ringBuffer = ringBuffer;
    }

    /**
     * Pushes a single item into the channel for consumption.
     * <p>
     * The method will publish the item to the ring buffer and
     * signal the consumer thread (if waiting) via
     * {@link Coordinator#wakeupConsumer()}.
     * </p>
     *
     * @param item the item to push into the channel
     */
    public void push(T item) {
        ringBuffer.push(coordinator, item);
        coordinator.wakeupConsumer();
    }

    /**
     * Pushes multiple items into the channel as a batch.
     * <p>
     * This allows for efficient batch publication by minimizing coordination
     * overhead between producers and consumers.
     * </p>
     *
     * @param items the array of items to push
     */
    public void push(T[] items) {
        ringBuffer.push(coordinator, items);
        coordinator.wakeupConsumer();
    }

    /**
     * Pushes multiple items into the channel as a batch.
     * <p>
     * This allows for efficient batch publication by minimizing coordination
     * overhead between producers and consumers.
     * </p>
     *
     * @param items the collection of items to push
     */
    public void push(Collection<T> items) {
        ringBuffer.push(coordinator, items);
        coordinator.wakeupConsumer();
    }

    /**
     * Signals the consumer that work is available, waking it if blocked or parked.
     */
    public void wakeupConsumer() {
        coordinator.wakeupConsumer();
    }

    /**
     * Attempts to receive up to {@code batchsize} items from the channel and
     * process them using the given {@link Consumer}.
     * <p>
     * This method is non-blocking and returns immediately, even if no items are
     * available. It is used in polling or event-loop style consumers.
     * </p>
     *
     * @param batchsize the maximum number of items to consume in one batch
     * @param consumer  the consumer function used to process received items
     */
    public void receive(int batchsize, Consumer<T> consumer) {
        if (ringBuffer.poll(batchsize, consumer) == PollerState.IDLE) {
            coordinator.consumerWait();
        }
    }

    /**
     * Continuously waits until at least one item is available to consume, then
     * processes up to {@code batchsize} items using the provided {@link Consumer}.
     * <p>
     * This method blocks using the {@link ConsumerWaitStrategy} defined in
     * the {@link Coordinator}. It is suitable for dedicated consumer threads.
     * </p>
     *
     * @param batchsize the maximum number of items to consume in one iteration
     * @param consumer  the consumer function used to process received items
     */
    public void blockingReceive(int batchsize, Consumer<T> consumer) {
        while (ringBuffer.poll(batchsize, consumer) == PollerState.IDLE) {
            coordinator.consumerWait();
        }
    }

    /**
     Returns the number of elements currently stored in the ring buffer.
     The returned value represents a snapshot at the time of the call and may change immediately if other threads are adding or removing elements concurrently.
     @return the current number of elements in the ring buffer
     */
    public long size() {
        return ringBuffer.size();
    }

    /**
     Frees holding off-heap memory by {@link AvailabilityBuffer}.
     */
    public void close() {
        ringBuffer.close();
    }

    private static ProducerWaitStrategy createProducerWaitStrategy(ProducerWaitStrategyType type) {
        ProducerWaitStrategy strategy = null;
        switch (type) {
            case PARKING:
                strategy = new ProducerParkingStrategy();
                break;
            case SPINNING:
                strategy = new ProducerSpinningStrategy();
                break;
            case YIELDING:
                strategy = new ProducerYieldingStrategy();
                break;
        }
        return strategy;
    }

    private static ConsumerWaitStrategy createConsumerWaitStrategy(ConsumerWaitStrategyType type) {
        ConsumerWaitStrategy strategy = null;
        switch (type) {
            case PARKING:
                strategy = new ConsumerParkingStrategy();
                break;
            case SPINNING:
                strategy = new ConsumerSpinningStrategy();
                break;
            case YIELDING:
                strategy = new ConsumerYieldingStrategy();
                break;
            case BLOCKING:
                strategy = new ConsumerBlockingStrategy();
                break;
        }
        return strategy;
    }

    /**
     * Creates a new single-producer, single-consumer (SPSC) channel with the given
     * capacity and wait strategies.
     *
     * @param capacity the size of the ring buffer
     * @param pw       the producer wait strategy type
     * @param cw       the consumer wait strategy type
     * @param <T>      the element type
     * @return a new SPSC {@code Channel}
     */
    public static <T> Channel<T> spsc(int capacity, ProducerWaitStrategyType pw, ConsumerWaitStrategyType cw) {
        Coordinator coordinator = new Coordinator(createProducerWaitStrategy(pw), createConsumerWaitStrategy(cw));
        Sequencer sequencer = new SingleProducerSequencer(capacity);
        Poller<T> poller = new SingleThreadPoller<>();
        return new Channel<>(coordinator, new RingBuffer<>(sequencer, poller, capacity));
    }

    /**
     * Creates a new multi-producer, single-consumer (MPSC) channel with the given
     * capacity and wait strategies.
     *
     * @param capacity the size of the ring buffer
     * @param pw       the producer wait strategy type
     * @param cw       the consumer wait strategy type
     * @param <T>      the element type
     * @return a new MPSC {@code Channel}
     */
    public static <T> Channel<T> mpsc(int capacity, ProducerWaitStrategyType pw, ConsumerWaitStrategyType cw) {
        Coordinator coordinator = new Coordinator(createProducerWaitStrategy(pw), createConsumerWaitStrategy(cw));
        Sequencer sequencer = new MultiProducerSequencer(capacity);
        Poller<T> poller = new SingleThreadPoller<>();
        return new Channel<>(coordinator, new RingBuffer<>(sequencer, poller, capacity));
    }

    /**
     * Creates a new single-producer, multi-consumer (SPMC) channel with the given
     * capacity and wait strategies.
     *
     * @param capacity the size of the ring buffer
     * @param pw       the producer wait strategy type
     * @param cw       the consumer wait strategy type
     * @param <T>      the element type
     * @return a new SPMC {@code Channel}
     */
    public static <T> Channel<T> spmc(int capacity, ProducerWaitStrategyType pw, ConsumerWaitStrategyType cw) {
        Coordinator coordinator = new Coordinator(createProducerWaitStrategy(pw), createConsumerWaitStrategy(cw));
        Sequencer sequencer = new SingleProducerSequencer(capacity);
        Poller<T> poller = new MultiThreadPoller<>();
        return new Channel<>(coordinator, new RingBuffer<>(sequencer, poller, capacity));
    }

    /**
     * Creates a new multi-producer, multi-consumer (MPMC) channel with the given
     * capacity and wait strategies.
     *
     * @param capacity the size of the ring buffer
     * @param pw       the producer wait strategy type
     * @param cw       the consumer wait strategy type
     * @param <T>      the element type
     * @return a new MPMC {@code Channel}
     */
    public static <T> Channel<T> mpmc(int capacity, ProducerWaitStrategyType pw, ConsumerWaitStrategyType cw) {
        Coordinator coordinator = new Coordinator(createProducerWaitStrategy(pw), createConsumerWaitStrategy(cw));
        Sequencer sequencer = new MultiProducerSequencer(capacity);
        Poller<T> poller = new MultiThreadPoller<>();
        return new Channel<>(coordinator, new RingBuffer<>(sequencer, poller, capacity));
    }

}
