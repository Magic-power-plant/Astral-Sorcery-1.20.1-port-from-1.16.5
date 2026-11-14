package hellfirepvp.astralsorcery.common.registry.extension;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ExtensionRegistryContractTest {
    private final ExtensionRegistry<String> registry = new ExtensionRegistry<>("test");
    private final ResourceLocation key = new ResourceLocation("addon", "entry");

    @Test void duplicateDoesNotReplaceTheOriginalOrRunTheFactory() {
        registry.register(key, "original");
        assertThrows(IllegalArgumentException.class, () -> registry.registerFactory(key, () -> {
            fail("Duplicate factory ran"); return "replacement";
        }));
        assertEquals("original", registry.get(key));
    }

    @Test void failedFactoryAndNullProductDoNotPublishAnything() {
        assertThrows(IllegalStateException.class, () -> registry.registerFactory(key, () -> {
            throw new IllegalStateException("broken");
        }));
        assertThrows(NullPointerException.class, () -> registry.register(key, null));
        assertTrue(registry.keys().isEmpty());
        registry.register(key, "retry");
        assertEquals("retry", registry.get(key));
    }

    @Test void idsAndFactoriesCannotBeNull() {
        assertThrows(NullPointerException.class, () -> registry.register(null, "value"));
        assertThrows(NullPointerException.class, () -> registry.registerFactory(key, null));
    }

    @Test void keySnapshotsAreImmutableAndDoNotChangeAfterRegistration() {
        registry.register(key, "first");
        List<ResourceLocation> snapshot = registry.keys();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        registry.register(new ResourceLocation("addon", "second"), "second");
        assertEquals(List.of(key), snapshot);
        assertEquals(2, registry.keys().size());
    }

    @Test void freezingPreventsAllFurtherRegistrationButKeepsReads() {
        registry.register(key, "original");
        registry.freeze();
        registry.freeze();
        assertThrows(IllegalStateException.class, () -> registry.registerFactory(
                new ResourceLocation("addon", "late"), () -> { fail("Late factory ran"); return "late"; }));
        assertEquals("original", registry.get(key));
    }

    @Test void competingRegistrationsRunOnlyOneFactory() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> register = () -> {
            start.await();
            try { registry.registerFactory(key, () -> { calls.incrementAndGet(); return "winner"; }); return true; }
            catch (IllegalArgumentException duplicate) { return false; }
        };
        try {
            Future<Boolean> one = executor.submit(register), two = executor.submit(register);
            start.countDown();
            assertNotEquals(one.get(5, TimeUnit.SECONDS), two.get(5, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
        } finally { executor.shutdownNow(); }
    }

    @Test void reentrantFactoryCannotOverwriteAnEntry() {
        assertThrows(IllegalArgumentException.class, () -> registry.registerFactory(key, () -> {
            registry.register(key, "nested"); return "outer";
        }));
        assertEquals("nested", registry.get(key));
    }
}
