package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.api.sync.SyncDataProvider;
import hellfirepvp.astralsorcery.common.registry.extension.ExtensionRegistry;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;

public final class SyncDataRegistry {
    private static final ExtensionRegistry<RegisteredSyncData<?, ?>> REGISTRY = new ExtensionRegistry<>("Sync data");
    private SyncDataRegistry() {}

    static void register(SyncDataProvider<?, ?> provider) {
        Objects.requireNonNull(provider, "provider");
        ResourceLocation key = Objects.requireNonNull(provider.getKey(), "provider key");
        REGISTRY.registerFactory(key, () -> new RegisteredSyncData<>(key, provider));
    }

    public static Collection<ResourceLocation> getKnownKeys() {
        return REGISTRY.keys();
    }

    @Nullable
    public static SyncDataProvider<?, ?> getProvider(ResourceLocation key) {
        RegisteredSyncData<?, ?> data = REGISTRY.get(key);
        return data == null ? null : data.provider;
    }

    @Nullable
    static RegisteredSyncData<?, ?> getData(ResourceLocation key) {
        return REGISTRY.get(key);
    }

    public static void freeze() {
        REGISTRY.freeze();
    }
}
