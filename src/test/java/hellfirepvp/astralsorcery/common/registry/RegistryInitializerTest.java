package hellfirepvp.astralsorcery.common.registry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RegistryInitializerTest {
    @Test
    void successfulInitializationRunsOnceAndKeepsPhaseStateIndependent() {
        AtomicInteger calls = new AtomicInteger();
        var first = phase(calls::incrementAndGet);
        var second = phase(calls::incrementAndGet);
        first.initialize();
        first.initialize();
        assertEquals(1, calls.get());
        second.initialize();
        second.initialize();
        assertEquals(2, calls.get());
    }

    @Test
    void partialFailurePreservesItsCauseAndNeverReplaysEntries() {
        List<String> entries = new ArrayList<>();
        var cause = new IllegalArgumentException("bad built-in entry");
        var initializer = phase(() -> { entries.add("first"); throw cause; });
        assertSame(cause, assertThrows(IllegalArgumentException.class, initializer::initialize));
        assertSame(cause, assertThrows(IllegalStateException.class, initializer::initialize).getCause());
        assertEquals(List.of("first"), entries);
    }

    @Test
    void errorsAlsoCloseInitialization() {
        var cause = new AssertionError("assembly failed");
        var initializer = phase(() -> { throw cause; });
        assertSame(cause, assertThrows(AssertionError.class, initializer::initialize));
        assertSame(cause, assertThrows(IllegalStateException.class, initializer::initialize).getCause());
    }

    @Test
    void reentrantInitializationFailsInsteadOfExposingIncompleteState() {
        var initializer = new AbstractRegistryInitializer() {
            @Override protected void registerEntries() { initialize(); }
        };
        var failure = assertThrows(IllegalStateException.class, initializer::initialize);
        assertTrue(failure.getMessage().contains("INITIALIZING"));
        assertSame(failure, assertThrows(IllegalStateException.class, initializer::initialize).getCause());
    }

    @Test
    void concurrentCallersAllObserveTheSameCompletedAssembly() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        List<String> entries = new ArrayList<>();
        var initializer = phase(() -> { calls.incrementAndGet(); entries.add("complete"); });
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(8);
        try {
            var results = new ArrayList<java.util.concurrent.Future<List<String>>>();
            for (int i = 0; i < 16; i++) {
                results.add(executor.submit(() -> {
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    initializer.initialize();
                    return List.copyOf(entries);
                }));
            }
            start.countDown();
            for (var result : results) assertEquals(List.of("complete"), result.get(5, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static AbstractRegistryInitializer phase(Runnable assembly) {
        return new AbstractRegistryInitializer() {
            @Override protected void registerEntries() { assembly.run(); }
        };
    }
}
