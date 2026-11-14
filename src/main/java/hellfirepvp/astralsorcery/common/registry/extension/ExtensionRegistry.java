package hellfirepvp.astralsorcery.common.registry.extension;

import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** Internal registry lifecycle: validate, construct, publish, then freeze before worlds can load. */
public final class ExtensionRegistry<T> {
    private final String name;
    private final Map<ResourceLocation, T> entries = new LinkedHashMap<>();
    private boolean frozen;

    public ExtensionRegistry(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public synchronized void register(ResourceLocation id, T value) {
        registerFactory(id, () -> value);
    }

    public synchronized void registerFactory(ResourceLocation id, Supplier<? extends T> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        checkAvailable(id);
        T value = Objects.requireNonNull(factory.get(), name + " factory returned null: " + id);
        // A factory may reenter the registry; never overwrite a nested registration.
        checkAvailable(id);
        entries.put(id, value);
    }

    private void checkAvailable(ResourceLocation id) {
        if (frozen) throw new IllegalStateException(name + " registration is closed: " + id);
        if (entries.containsKey(id)) throw new IllegalArgumentException(name + " duplicate identifier: " + id);
    }

    @Nullable
    public synchronized T get(ResourceLocation id) {
        return entries.get(id);
    }

    public synchronized List<ResourceLocation> keys() {
        return List.copyOf(entries.keySet());
    }

    public synchronized void freeze() {
        frozen = true;
    }
}
