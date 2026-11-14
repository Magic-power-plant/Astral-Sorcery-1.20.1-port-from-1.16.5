package hellfirepvp.astralsorcery.common.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/** Typed bridge from early domain construction to the Forge registry events. */
public final class ForgeRegistryHandle<T> extends AbstractBootstrapRegistry<T> {
    private final boolean modifiable;
    private IForgeRegistry<T> registry;
    private boolean creationRequested;

    public ForgeRegistryHandle(ResourceLocation name, Function<? super T, ResourceLocation> identifier) {
        this(name, identifier, false);
    }

    public ForgeRegistryHandle(ResourceLocation name, Function<? super T, ResourceLocation> identifier,
                               boolean modifiable) {
        super(name, identifier);
        this.modifiable = modifiable;
    }

    public synchronized void create(NewRegistryEvent event) {
        if (creationRequested) {
            throw new IllegalStateException("Registry already created: " + getRegistryName());
        }
        creationRequested = true;
        RegistryBuilder<T> builder = new RegistryBuilder<T>().setName(getRegistryName()).disableSaving();
        // Preserve the original Forge sync/modification policy for perk converters and modifiers.
        if (modifiable) builder.allowModification();
        else builder.disableSync();
        // NewRegistryEvent's supplier is not populated during the event. Bind only in onFill.
        event.create(builder, this::bind);
    }

    private synchronized void bind(IForgeRegistry<T> created) {
        Objects.requireNonNull(created, "registry");
        if (registry != null || !getRegistryName().equals(created.getRegistryName())) {
            throw new IllegalStateException("Unexpected registry binding: " + getRegistryName());
        }
        registry = created;
    }

    public synchronized void registerEntries(RegisterEvent event) {
        if (!event.getRegistryKey().location().equals(getRegistryName())) return;
        if (registry == null) {
            throw new IllegalStateException("Registry has not been created: " + getRegistryName());
        }
        publishEntries((id, value) -> event.register(registry.getRegistryKey(), id, () -> value));
    }

    @Override
    protected T getPublishedValue(ResourceLocation id) {
        return registry.getValue(id);
    }

    @Override
    protected ResourceLocation getPublishedKey(T value) {
        return registry.getKey(value);
    }

    @Override
    protected Collection<T> getPublishedValues() {
        return registry.getValues();
    }

    @Override
    protected Set<ResourceLocation> getPublishedKeys() {
        return registry.getKeys();
    }
}
