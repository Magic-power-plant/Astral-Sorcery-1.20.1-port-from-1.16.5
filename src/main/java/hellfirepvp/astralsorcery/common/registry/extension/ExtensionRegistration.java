package hellfirepvp.astralsorcery.common.registry.extension;

import hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataRegistry;
import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Completes registration after all mod constructors and Forge registries, before any world loads. */
public final class ExtensionRegistration {
    private static final ExtensionRegistration INSTANCE = new ExtensionRegistration();
    private final AtomicBoolean dispatched = new AtomicBoolean();

    // Separate lifecycle instances let tests dispatch without freezing global registries.
    ExtensionRegistration() {}

    public static void complete() {
        INSTANCE.dispatch(MinecraftForge.EVENT_BUS::post, TransmissionClassRegistry::freeze,
                SourceClassRegistry::freeze, SyncDataRegistry::freeze);
    }

    void dispatch(Consumer<Event> post, Runnable freezeTransmission, Runnable freezeSource, Runnable freezeSync) {
        if (!dispatched.compareAndSet(false, true)) {
            throw new IllegalStateException("Extensions already registered");
        }
        try {
            post.accept(new StarlightNetworkEvent.TransmissionRegister(TransmissionClassRegistry.eventInstance));
            post.accept(new StarlightNetworkEvent.SourceProviderRegistry(SourceClassRegistry.eventInstance));
            RegisterAstralSorceryExtensionsEvent event = new RegisterAstralSorceryExtensionsEvent();
            try {
                post.accept(event);
            } finally {
                event.finishDispatch();
            }
        } finally {
            try {
                freezeTransmission.run();
            } finally {
                try {
                    freezeSource.run();
                } finally {
                    freezeSync.run();
                }
            }
        }
    }
}
