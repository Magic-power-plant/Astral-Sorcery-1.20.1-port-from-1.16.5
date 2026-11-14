package hellfirepvp.astralsorcery.common.registry;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class BootstrapRegistryContractTest {
    private static final ResourceLocation FIRST = ResourceLocation.parse("test:first");
    private static final ResourceLocation SECOND = ResourceLocation.parse("addon:second");
    private record Entry(ResourceLocation id, String value) {}

    private static final class TestRegistry extends AbstractBootstrapRegistry<Entry> {
        private final Map<ResourceLocation, Entry> backing = new LinkedHashMap<>();

        TestRegistry() { super(ResourceLocation.parse("test:entries"), Entry::id); }
        void publish() { publishEntries(backing::put); }
        void publish(BiConsumer<ResourceLocation, Entry> sink) { publishEntries(sink); }
        @Override protected Entry getPublishedValue(ResourceLocation id) { return backing.get(id); }
        @Override protected ResourceLocation getPublishedKey(Entry value) {
            return backing.entrySet().stream().filter(e -> e.getValue() == value)
                    .map(Map.Entry::getKey).findFirst().orElse(null);
        }
        @Override protected Collection<Entry> getPublishedValues() { return backing.values(); }
        @Override protected Set<ResourceLocation> getPublishedKeys() { return backing.keySet(); }
    }

    @Test
    void constructionAndPublicationPreserveIdentityAndIterationOrder() {
        TestRegistry registry = new TestRegistry();
        Entry first = registry.register(new Entry(FIRST, "first"));
        Entry second = registry.register(new Entry(SECOND, "second"));
        RegistryView<Entry> view = registry;
        assertSame(first, view.getValue(FIRST));
        assertEquals(FIRST, view.getKey(first));
        assertEquals(List.of(first, second), view.getValues());
        registry.publish();
        assertSame(first, view.getValue(FIRST));
        assertSame(second, view.getValue(SECOND));
        assertEquals(List.of(first, second), view.getValues());
        assertEquals(FIRST, view.getKey(first));
    }

    @Test
    void liveRegistryChangesAreVisibleAndNeverFallBackToStaleBuiltins() {
        TestRegistry registry = new TestRegistry();
        Entry original = registry.register(new Entry(FIRST, "original"));
        registry.publish();
        Entry override = new Entry(FIRST, "override");
        Entry addon = new Entry(SECOND, "addon");
        registry.backing.put(FIRST, override);
        registry.backing.put(SECOND, addon);
        assertSame(override, registry.getValue(FIRST));
        assertSame(addon, registry.getValue(SECOND));
        assertNull(registry.getKey(original));
        assertEquals(List.of(override, addon), registry.getValues());
        assertEquals(Set.of(FIRST, SECOND), registry.getKeys());
        registry.backing.remove(FIRST);
        assertNull(registry.getValue(FIRST), "must not resurrect the old built-in");
        assertFalse(registry.containsKey(FIRST));
    }

    @Test
    void snapshotsAndIteratorsCannotMutateTheRegistryAndDoNotChangeLater() {
        TestRegistry registry = new TestRegistry();
        Entry first = registry.register(new Entry(FIRST, "first"));
        List<Entry> values = registry.getValues();
        Set<ResourceLocation> keys = registry.getKeys();
        assertThrows(UnsupportedOperationException.class, values::clear);
        assertThrows(UnsupportedOperationException.class, keys::clear);
        var iterator = registry.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        registry.register(new Entry(SECOND, "second"));
        registry.publish();
        assertEquals(List.of(first), values);
        assertEquals(Set.of(FIRST), keys);
        assertEquals(2, registry.getValues().size());
    }

    @Test
    void invalidAndDuplicateEntriesDoNotReplaceTheOriginal() {
        TestRegistry registry = new TestRegistry();
        Entry first = registry.register(new Entry(FIRST, "first"));
        assertThrows(NullPointerException.class, () -> registry.register(null));
        assertThrows(NullPointerException.class, () -> registry.register(new Entry(null, "invalid")));
        assertThrows(IllegalArgumentException.class, () -> registry.register(new Entry(FIRST, "other")));
        assertSame(first, registry.getValue(FIRST));
        assertNull(registry.getValue(null));
        assertNull(registry.getKey(new Entry(FIRST, "first")), "reverse lookup uses object identity");
    }

    @Test
    void registrationAndRepeatPublicationAreRejectedAfterPublicationStarts() {
        TestRegistry registry = new TestRegistry();
        Entry first = registry.register(new Entry(FIRST, "first"));
        registry.publish((id, value) -> {
            assertSame(first, registry.getValue(FIRST));
            assertThrows(IllegalStateException.class, () -> registry.register(new Entry(SECOND, "late")));
            assertThrows(IllegalStateException.class, registry::publish);
            registry.backing.put(id, value);
        });
        assertThrows(IllegalStateException.class, () -> registry.register(new Entry(SECOND, "late")));
        assertThrows(IllegalStateException.class, registry::publish);
        assertSame(first, registry.getValue(FIRST));
    }

    @Test
    void partialPublicationFailsClosedAndCannotBeRetried() {
        TestRegistry registry = new TestRegistry();
        registry.register(new Entry(FIRST, "first"));
        assertThrows(IllegalArgumentException.class, () -> registry.publish((id, value) -> {
            registry.backing.put(id, value);
            throw new IllegalArgumentException("backend failed");
        }));
        assertThrows(IllegalStateException.class, registry::publish);
        assertThrows(IllegalStateException.class, () -> registry.getValue(FIRST));
        assertThrows(IllegalStateException.class, registry::getValues);
        assertThrows(IllegalStateException.class, registry::getKeys);
        assertThrows(IllegalStateException.class, () -> registry.register(new Entry(SECOND, "late")));
    }

    @Test
    void anEmptyPublishedRegistryStillSeesSubsequentAddonEntries() {
        TestRegistry registry = new TestRegistry();
        registry.publish();
        Entry addon = new Entry(SECOND, "addon");
        registry.backing.put(SECOND, addon);
        assertSame(addon, registry.getValue(SECOND));
    }

    @Test
    void concurrentDuplicateRegistrationsHaveExactlyOneWinner() throws Exception {
        TestRegistry registry = new TestRegistry();
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        java.util.concurrent.Callable<Boolean> register = () -> {
            start.await();
            try { registry.register(new Entry(FIRST, "winner")); return true; }
            catch (IllegalArgumentException duplicate) { return false; }
        };
        try {
            var first = executor.submit(register);
            var second = executor.submit(register);
            start.countDown();
            assertNotEquals(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(1, registry.getValues().size());
        } finally {
            executor.shutdownNow();
        }
    }
}
