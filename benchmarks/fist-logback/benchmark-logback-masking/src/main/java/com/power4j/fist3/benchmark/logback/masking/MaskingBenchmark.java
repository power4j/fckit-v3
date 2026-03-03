package com.power4j.fist3.benchmark.logback.masking;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.core.ProcessorChain;
import com.power4j.fist.logback.core.RuleEngine;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class MaskingBenchmark {

    @Param({ "plain message without any sensitive data",
            "用户手机号: 13812345678, 请注意保密" })
    private String message;

    private ProcessorChain chain;
    private LogMessageContext ctx;

    @Setup
    public void setup() {
        LoggerContext loggerContext = new LoggerContext();

        RuleEngine engine = new RuleEngine();
        engine.setContext(loggerContext);

        chain = new ProcessorChain(List.of(engine), Collections.singletonMap("configFile", "benchmark-masking.properties"));
        chain.setContext(loggerContext);
        chain.start();

        ctx = new LogMessageContext(Level.INFO, "benchmark", "main", System.currentTimeMillis(), null, () -> null);
    }

    @TearDown
    public void tearDown() {
        chain.destroy();
    }

    @Benchmark
    public void baseline(Blackhole bh) {
        bh.consume(message);
    }

    @Benchmark
    public void withMasking(Blackhole bh) {
        bh.consume(chain.execute(message, ctx));
    }

}
