package hellfirepvp.astralsorcery.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/** Read-only lookup contract for a keyed extension registry. */
public interface ExtensionPoint<T> {

    T get(ResourceLocation id);

    Set<ResourceLocation> keys();

    ResourceLocation getKey();
}
