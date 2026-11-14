package hellfirepvp.astralsorcery.api.event;

import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.common.data.sync.SyncDataHolder;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.SourceClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionClassRegistry;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import java.util.Objects;

/** Posted once on MinecraftForge.EVENT_BUS during enqueued common setup.
 * Register synchronously in the callback; all three registries freeze immediately after dispatch.
 * Not cancelable, not a mod-bus event, and not a datapack reload hook. */
public final class RegisterAstralSorceryExtensionsEvent extends Event {
    public void registerTransmission(TransmissionProvider provider) {
        checkId(Objects.requireNonNull(provider).getIdentifier());
        TransmissionClassRegistry.register(provider);
    }

    public void registerSource(SourceClassRegistry.SourceProvider provider) {
        checkId(Objects.requireNonNull(provider).getIdentifier());
        SourceClassRegistry.register(provider);
    }

    public void registerSyncData(SyncDataProvider<?, ?> provider) {
        checkId(Objects.requireNonNull(provider).getKey());
        SyncDataHolder.register(provider);
    }

    private static void checkId(ResourceLocation id) {
        Objects.requireNonNull(id, "extension identifier");
        if ("astralsorcery".equals(id.getNamespace()) || "minecraft".equals(id.getNamespace())) {
            throw new IllegalArgumentException("Extensions must use their own namespace: " + id);
        }
    }
}
