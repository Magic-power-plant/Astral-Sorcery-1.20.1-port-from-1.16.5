package hellfirepvp.astralsorcery.common.starlight.transmission.registry;

import hellfirepvp.astralsorcery.common.registry.extension.ExtensionRegistry;
import hellfirepvp.astralsorcery.common.starlight.IIndependentStarlightSource;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.crystal.IndependentCrystalSource;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SourceClassRegistry {

    public static final SourceClassRegistry eventInstance = new SourceClassRegistry();

    private static final ExtensionRegistry<SourceProvider> providerMap = new ExtensionRegistry<>("SourceProvider");

    private SourceClassRegistry() {}

    public void registerProvider(SourceProvider provider) {
        register(provider);
    }

    @Nullable
    public static SourceProvider getProvider(ResourceLocation identifier) {
        return providerMap.get(identifier);
    }

    public static void register(SourceProvider provider) {
        Objects.requireNonNull(provider, "provider");
        providerMap.register(provider.getIdentifier(), provider);
    }

    public static void freeze() {
        providerMap.freeze();
    }

    public static void setupRegistry() {
        register(new IndependentCrystalSource.Provider());

        // Extension events are dispatched later, during enqueued common setup.
    }

    public interface SourceProvider {
        IIndependentStarlightSource provideEmptySource();

        @Nonnull
        ResourceLocation getIdentifier();
    }
}
