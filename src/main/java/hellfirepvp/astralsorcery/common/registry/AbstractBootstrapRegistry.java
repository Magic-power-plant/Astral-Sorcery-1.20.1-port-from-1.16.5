package hellfirepvp.astralsorcery.common.registry;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Owns the construction-to-publication lifecycle of a mod registry.
 * Pending entries support construction-time queries. Once published, the backing registry is
 * authoritative, including additions, replacements and removals; the pending index is discarded.
 * Config/reload registries and the separate extension-registration window do not use this lifecycle.
 */
public abstract class AbstractBootstrapRegistry<T> implements RegistryView<T> {
    private enum State { COLLECTING, PUBLISHING, LIVE, FAILED }

    private final ResourceLocation registryName;
    private final Function<? super T, ResourceLocation> identifier;
    private final Map<ResourceLocation, T> pending = new LinkedHashMap<>();
    private State state = State.COLLECTING;

    protected AbstractBootstrapRegistry(ResourceLocation registryName,
                                        Function<? super T, ResourceLocation> identifier) {
        this.registryName = Objects.requireNonNull(registryName, "registryName");
        this.identifier = Objects.requireNonNull(identifier, "identifier");
    }

    public final ResourceLocation getRegistryName() {
        return registryName;
    }

    public final synchronized <S extends T> S register(S value) {
        checkCollecting();
        Objects.requireNonNull(value, "value");
        ResourceLocation id = Objects.requireNonNull(identifier.apply(value), "entry identifier");
        // Identifier functions can reenter the registry; validate again before publishing the entry.
        checkCollecting();
        if (pending.putIfAbsent(id, value) != null) {
            throw new IllegalArgumentException(registryName + " duplicate identifier: " + id);
        }
        return value;
    }

    protected final synchronized void publishEntries(BiConsumer<ResourceLocation, T> registrar) {
        Objects.requireNonNull(registrar, "registrar");
        checkCollecting();
        state = State.PUBLISHING;
        try {
            pending.forEach(registrar);
            pending.clear();
            state = State.LIVE;
        } catch (RuntimeException | Error failure) {
            // Publication may have partially mutated Forge. Never retry or expose a partial registry.
            state = State.FAILED;
            throw failure;
        }
    }

    private void checkCollecting() {
        if (state != State.COLLECTING) {
            throw new IllegalStateException(registryName + " registration is closed: " + state);
        }
    }

    private void checkReadable() {
        if (state == State.FAILED) {
            throw new IllegalStateException(registryName + " publication failed");
        }
    }

    @Nullable
    @Override
    public final synchronized T getValue(@Nullable ResourceLocation id) {
        checkReadable();
        if (id == null) return null;
        return state == State.LIVE ? getPublishedValue(id) : pending.get(id);
    }

    @Nullable
    @Override
    public final synchronized ResourceLocation getKey(T value) {
        checkReadable();
        if (value == null) return null;
        if (state == State.LIVE) return getPublishedKey(value);
        for (Map.Entry<ResourceLocation, T> entry : pending.entrySet()) {
            if (entry.getValue() == value) return entry.getKey();
        }
        return null;
    }

    @Override
    public final synchronized List<T> getValues() {
        checkReadable();
        return List.copyOf(state == State.LIVE ? getPublishedValues() : pending.values());
    }

    @Override
    public final synchronized Set<ResourceLocation> getKeys() {
        checkReadable();
        return Collections.unmodifiableSet(new LinkedHashSet<>(
                state == State.LIVE ? getPublishedKeys() : pending.keySet()));
    }

    protected abstract T getPublishedValue(ResourceLocation id);
    protected abstract ResourceLocation getPublishedKey(T value);
    protected abstract java.util.Collection<T> getPublishedValues();
    protected abstract Set<ResourceLocation> getPublishedKeys();
}
