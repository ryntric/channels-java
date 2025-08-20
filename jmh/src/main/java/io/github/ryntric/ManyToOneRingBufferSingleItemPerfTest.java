package io.github.ryntric;

import io.github.ryntric.EventTranslator.EventTranslatorOneArg;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

/**
 * author: ryntric
 * date: 8/11/25
 * time: 1:45 PM
 **/

@Fork(1)
@Warmup(iterations = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class ManyToOneRingBufferSingleItemPerfTest {
    private static final EventTranslatorOneArg<Event, Object> TRANSLATOR = Event::setPayload;
    private static final Object DUMMY_VALUE = new Object();
    private static final EventHandler<Event> HANDLER = new EventHandler<>() {
        @Override
        public void onEvent(Event event, long sequence) {
        }

        @Override
        public void onError(Throwable ex) {

        }

        @Override
        public void onStart() {
        }

        @Override
        public void onShutdown() {
        }
    };

    @State(Scope.Group)
    public static class RingBufferState {
        private final PollerWaitPolicy pollerPolicy = PollerWaitPolicyFactory.create(PollerWaitPolicyType.PARKING);
        private final ProducerWaitPolicy producerPolicy = ProducerWaitPolicyFactory.create(ProducerWaitPolicyType.PARKING);
        private final RingBuffer<Event> ringBuffer = new RingBuffer<>(Event::new, SequencerType.MULTI_PRODUCER, pollerPolicy, producerPolicy,1 << 12);
        private final EventPoller<Event> eventPoller = new EventPoller<>("worker-test", new ThreadGroup("test"), ringBuffer, pollerPolicy, HANDLER, BatchSizeLimit._1_2);

        @Setup
        public void setup() {
            eventPoller.start();
        }

        @TearDown
        public void teardown() {
            eventPoller.shutdown();
        }
    }

    @Benchmark
    @Group("manyToOne")
    public void producer1(RingBufferState state) {
        state.ringBuffer.publishEvent(TRANSLATOR, DUMMY_VALUE);
    }

    @Benchmark
    @Group("manyToOne")
    public void producer2(RingBufferState state) {
        state.ringBuffer.publishEvent(TRANSLATOR, DUMMY_VALUE);
    }

    @Benchmark
    @Group("manyToOne")
    public void producer3(RingBufferState state) {
        state.ringBuffer.publishEvent(TRANSLATOR, DUMMY_VALUE);
    }

    @Benchmark
    @Group("manyToOne")
    public void producer4(RingBufferState state) {
        state.ringBuffer.publishEvent(TRANSLATOR, DUMMY_VALUE);
    }

    public static class Event {
        private Object payload;

        public void setPayload(Object payload) {
            this.payload = payload;
        }
    }

}
