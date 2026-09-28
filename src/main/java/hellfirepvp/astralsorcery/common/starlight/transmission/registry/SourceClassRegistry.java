package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.event.StarlightNetworkEvent;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.IndependentCrystalSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SourceClassRegistry {

    public static final SourceClassRegistry eventInstance = new SourceClassRegistry();

    private static final Map<ResourceLocation, SourceProvider> providerMap = new HashMap<>();

    private SourceClassRegistry() {}

    public void registerProvider(SourceProvider provider) {
        register(provider);
    }

    @Nullable
    public static SourceProvider getProvider(ResourceLocation identifier) {
        return providerMap.get(identifier);
    }

    public static void register(SourceProvider provider) {
        if (providerMap.containsKey(provider.getIdentifier())) {
            throw new IllegalArgumentException("Already registered identifier SourceProvider: " + provider.getIdentifier());
        }
        providerMap.put(provider.getIdentifier(), provider);
    }

    public static void setupRegistry() {
        register(new IndependentCrystalSource.Provider());

        //1.16.5 SourceClassRegistry.java:56 - see the note in TransmissionClassRegistry#setupRegistry.
        MinecraftForge.EVENT_BUS.post(new StarlightNetworkEvent.SourceProviderRegistry(eventInstance));
    }

    public interface SourceProvider {
        IIndependentStarlightSource provideEmptySource();

        @Nonnull
        ResourceLocation getIdentifier();
    }
}
