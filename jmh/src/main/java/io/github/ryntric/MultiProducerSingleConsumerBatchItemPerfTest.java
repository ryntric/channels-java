package io.github.ryntric;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Fork(1)
@Warmup(iterations = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class MultiProducerSingleConsumerBatchItemPerfTest {
       private static final Object[] DUMMIES = {
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
            new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
    };
    private static final AtomicBoolean isRunning = new AtomicBoolean(true);

    @State(Scope.Group)
    public static class OneToOneRingBufferState {
        private final Channel<Object> channel = Channel.mpsc(8192, ProducerWaitStrategyType.SPINNING, ConsumerWaitStrategyType.SPINNING);

        @Setup
        public void setup(Blackhole bh) {
            new Thread(() -> {
                Consumer<Object> handler = bh::consume;
                while (isRunning.getOpaque()) {
                    channel.blockingReceive(2048, handler);
                }
            }).start();

        }

        @TearDown
        public void teardown() {
            isRunning.setRelease(false);
        }
    }

    @Benchmark
    @Group("multiProducerSingleConsumer")
    @OperationsPerInvocation(64)
    public void producer1(OneToOneRingBufferState state) {
        state.channel.push(DUMMIES);
    }

    @Benchmark
    @Group("multiProducerSingleConsumer")
    @OperationsPerInvocation(64)
    public void producer2(OneToOneRingBufferState state) {
        state.channel.push(DUMMIES);
    }

    @Benchmark
    @Group("multiProducerSingleConsumer")
    @OperationsPerInvocation(64)
    public void producer3(OneToOneRingBufferState state) {
        state.channel.push(DUMMIES);
    }

    @Benchmark
    @Group("multiProducerSingleConsumer")
    @OperationsPerInvocation(64)
    public void producer4(OneToOneRingBufferState state) {
        state.channel.push(DUMMIES);
    }

}
