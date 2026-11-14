package hellfirepvp.astralsorcery.common.registry.extension;

import hellfirepvp.astralsorcery.api.event.RegisterAstralSorceryExtensionsEvent;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataRegistry;
import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import net.minecraftforge.common.MinecraftForge;

/** Completes registration after all mod constructors and Forge registries, before any world loads. */
public final class ExtensionRegistration {
    private static boolean dispatched;
    private ExtensionRegistration() {}

    public static synchronized void complete() {
        if (dispatched) throw new IllegalStateException("Extensions already registered");
        dispatched = true;
        try {
            MinecraftForge.EVENT_BUS.post(new StarlightNetworkEvent.TransmissionRegister(TransmissionClassRegistry.eventInstance));
            MinecraftForge.EVENT_BUS.post(new StarlightNetworkEvent.SourceProviderRegistry(SourceClassRegistry.eventInstance));
            MinecraftForge.EVENT_BUS.post(new RegisterAstralSorceryExtensionsEvent());
        } finally {
            TransmissionClassRegistry.freeze();
            SourceClassRegistry.freeze();
            SyncDataRegistry.freeze();
        }
    }
}
