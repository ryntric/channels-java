package io.github.ryntric;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.ZZZZ_Result;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

@State
@JCStressTest
@Outcome(id = "true, true, true, true", expect = Expect.ACCEPTABLE)
public class MultiProducerMultiConsumerSingleItemStressTest {
    private final Channel<Object> channel = Channel.mpmc(8192, ProducerWaitStrategyType.SPINNING, ConsumerWaitStrategyType.SPINNING);
    private final Set<Object> pushed = ConcurrentHashMap.newKeySet();
    private final Set<Object> processed = ConcurrentHashMap.newKeySet();

    private final LongAdder produced = new LongAdder();
    private final LongAdder consumed = new LongAdder();
    private final Consumer<Object> handler = obj -> {
        Objects.requireNonNull(obj);
        if (pushed.contains(obj) && processed.add(obj)) {
            consumed.increment();
            return;
        }
        throw new RuntimeException("processed already contains its value");
    };

    @Actor
    public void producer1() {
        Object item = new Object();
        pushed.add(item);
        channel.push(item);
        produced.increment();
    }

    @Actor
    public void producer2() {
        Object item = new Object();
        pushed.add(item);
        channel.push(item);
        produced.increment();
    }

    @Actor
    public void producer3() {
        Object item = new Object();
        pushed.add(item);
        channel.push(item);
        produced.increment();
    }

    @Actor
    public void producer4() {
        Object item = new Object();
        pushed.add(item);
        channel.push(item);
        produced.increment();
    }

    @Actor
    public void consumer1(ZZZZ_Result result) {
        while (consumed.longValue() != produced.longValue()) {
            channel.receive(2048, handler);
        }
        result.r1 = true;
    }

    @Actor
    public void consumer2(ZZZZ_Result result) {
        while (consumed.longValue() != produced.longValue()) {
            channel.receive(2048, handler);
        }
        result.r2 = true;
    }

    @Actor
    public void consumer3(ZZZZ_Result result) {
        while (consumed.longValue() != produced.longValue()) {
            channel.receive(2048, handler);
        }
        result.r3 = true;
    }

    @Actor
    public void consumer4(ZZZZ_Result result) {
        while (consumed.longValue() != produced.longValue()) {
            channel.receive(2048, handler);
        }
        result.r4 = true;
    }

}
