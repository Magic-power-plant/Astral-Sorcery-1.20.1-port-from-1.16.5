package hellfirepvp.astralsorcery.common.registry;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Read-only lookup capability, independent of the registry's registration lifecycle. */
public interface RegistryView<T> extends Iterable<T> {
    @Nullable
    T getValue(@Nullable ResourceLocation id);

    @Nullable
    ResourceLocation getKey(T value);

    List<T> getValues();

    Set<ResourceLocation> getKeys();

    default boolean containsKey(ResourceLocation id) {
        return getValue(id) != null;
    }

    @Override
    default Iterator<T> iterator() {
        return getValues().iterator();
    }
}
