package hellfirepvp.astralsorcery.common.data.sync;

import hellfirepvp.astralsorcery.common.data.sync.base.AbstractDataProvider;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SyncDataRegistry {

    private static final Map<ResourceLocation, AbstractDataProvider<?, ?>> REGISTRY = new HashMap<>();

    private SyncDataRegistry() {}

    static void register(AbstractDataProvider<?, ?> provider) {
        REGISTRY.put(provider.getKey(), provider);
    }

    public static Collection<ResourceLocation> getKnownKeys() {
        return REGISTRY.keySet();
    }

    @Nullable
    public static AbstractDataProvider<?, ?> getProvider(ResourceLocation key) {
        return REGISTRY.get(key);
    }

}
