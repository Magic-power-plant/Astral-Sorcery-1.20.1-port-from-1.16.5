package hellfirepvp.astralsorcery.common.registry.extension;

import hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent;
import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import net.minecraftforge.eventbus.api.Event;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionRegistrationLifecycleTest {
    @Test void compatibilityEventsPrecedeTheUnifiedEventAndFreezeOnlyAfterClosingIt() {
        ExtensionRegistration lifecycle = new ExtensionRegistration();
        List<Class<?>> events = new ArrayList<>();
        List<String> frozen = new ArrayList<>();
        AtomicReference<RegisterAstralSorceryExtensionsEvent> retained = new AtomicReference<>();
        lifecycle.dispatch(event -> {
            assertTrue(frozen.isEmpty());
            events.add(event.getClass());
            if (event instanceof RegisterAstralSorceryExtensionsEvent unified) {
                retained.set(unified);
                assertFalse(unified.isCancelable());
                assertThrows(NullPointerException.class, () -> unified.registerTransmission(null));
            }
        }, () -> {
            assertClosed(retained.get());
            frozen.add("transmission");
        }, () -> frozen.add("source"), () -> frozen.add("sync"));
        assertEquals(List.of(StarlightNetworkEvent.TransmissionRegister.class,
                StarlightNetworkEvent.SourceProviderRegistry.class,
                RegisterAstralSorceryExtensionsEvent.class), events);
        assertEquals(List.of("transmission", "source", "sync"), frozen);
        assertThrows(IllegalStateException.class, () -> lifecycle.dispatch(event -> fail(),
                () -> fail(), () -> fail(), () -> fail()));
    }

    @Test void eachCallbackFailureClosesRegistrationAndCannotBeRetried() {
        for (int failureIndex = 0; failureIndex < 3; failureIndex++) {
            ExtensionRegistration lifecycle = new ExtensionRegistration();
            List<Event> events = new ArrayList<>();
            List<String> frozen = new ArrayList<>();
            int failAt = failureIndex;
            IllegalArgumentException failure = new IllegalArgumentException("addon failed");
            assertSame(failure, assertThrows(IllegalArgumentException.class, () -> lifecycle.dispatch(event -> {
                events.add(event);
                if (events.size() == failAt + 1) throw failure;
            }, () -> frozen.add("transmission"), () -> frozen.add("source"), () -> frozen.add("sync"))));
            assertEquals(failAt + 1, events.size());
            assertEquals(List.of("transmission", "source", "sync"), frozen);
            if (events.get(events.size() - 1) instanceof RegisterAstralSorceryExtensionsEvent unified) {
                assertClosed(unified);
            }
            assertThrows(IllegalStateException.class, () -> lifecycle.dispatch(event -> fail(),
                    () -> fail(), () -> fail(), () -> fail()));
        }
    }

    @Test void reentrantDispatchFailsImmediatelyWithoutDispatchingOrFreezingAgain() {
        ExtensionRegistration lifecycle = new ExtensionRegistration();
        List<String> frozen = new ArrayList<>();
        lifecycle.dispatch(event -> assertThrows(IllegalStateException.class, () -> lifecycle.dispatch(
                nested -> fail(), () -> fail(), () -> fail(), () -> fail())),
                () -> frozen.add("transmission"), () -> frozen.add("source"), () -> frozen.add("sync"));
        assertEquals(3, frozen.size());
    }

    @Test void laterRegistriesStillFreezeIfAnEarlierFreezerFails() {
        List<String> frozen = new ArrayList<>();
        assertThrows(IllegalStateException.class, () -> new ExtensionRegistration().dispatch(event -> {},
                () -> { frozen.add("transmission"); throw new IllegalStateException("freeze failed"); },
                () -> frozen.add("source"), () -> frozen.add("sync")));
        assertEquals(List.of("transmission", "source", "sync"), frozen);
    }

    @Test void asynchronousRegistrationIsRejectedBeforeReadingAnyProvider() throws Exception {
        var executor = Executors.newSingleThreadExecutor();
        RegisterAstralSorceryExtensionsEvent event = new RegisterAstralSorceryExtensionsEvent();
        try {
            executor.submit(() -> assertClosed(event)).get(5, TimeUnit.SECONDS);
            // It remains open on the dispatch thread after the rejected call.
            assertThrows(NullPointerException.class, () -> event.registerSyncData(null));
        } finally {
            event.finishDispatch();
            executor.shutdownNow();
        }
    }

    private static void assertClosed(RegisterAstralSorceryExtensionsEvent event) {
        assertNotNull(event);
        assertThrows(IllegalStateException.class, () -> event.registerTransmission(null));
        assertThrows(IllegalStateException.class, () -> event.registerSource(null));
        assertThrows(IllegalStateException.class, () -> event.registerSyncData(null));
    }
}
