package hellfirepvp.astralsorcery.api.event;

import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Posted once on MinecraftForge.EVENT_BUS during enqueued common setup.
 * Register synchronously in the callback; the event closes and all three registries freeze
 * immediately after dispatch, even when a callback fails. Not cancelable or a mod-bus event.
 * Constructing or posting this event yourself is not a supported registration lifecycle.
 */
public final class RegisterAstralSorceryExtensionsEvent extends Event {
    private final Thread owner = Thread.currentThread();
    private final AtomicBoolean open = new AtomicBoolean(true);

    private void requireOpen() {
        if (!open.get()) {
            throw new IllegalStateException("Extension registration event is closed");
        }
        if (Thread.currentThread() != owner) {
            throw new IllegalStateException("Extensions must register on the event dispatch thread");
        }
    }

    /** Framework lifecycle hook; addon code must not close the registration event. */
    public final void finishDispatch() {
        open.set(false);
    }
    /** Registers a lazy node factory with a stable ID in the addon's own namespace. */
    public void registerTransmission(TransmissionProvider provider) {
        requireOpen();
        TransmissionClassRegistry.registerExtension(provider);
    }

    /** Registers a lazy independent-source factory in the addon's own namespace. */
    public void registerSource(SourceClassRegistry.SourceProvider provider) {
        requireOpen();
        SourceClassRegistry.registerExtension(provider);
    }

    /** Constructs and atomically publishes one common-side-safe sync channel. */
    public void registerSyncData(SyncDataProvider<?, ?> provider) {
        requireOpen();
        SyncDataHolder.registerExtension(provider);
    }

    /** Registers a constellation under an ID in the calling addon's namespace. */
    public void registerConstellation(IConstellation constellation) {
        requireOpen();
        java.util.Objects.requireNonNull(constellation, "constellation");
        net.minecraft.resources.ResourceLocation id = java.util.Objects.requireNonNull(
                constellation.getRegistryName(), "constellation ID");
        if ("astralsorcery".equals(id.getNamespace()) || "minecraft".equals(id.getNamespace())) {
            throw new IllegalArgumentException("Extensions must use their own namespace: " + id);
        }
        ConstellationRegistry.registerExtension(constellation);
    }
}
