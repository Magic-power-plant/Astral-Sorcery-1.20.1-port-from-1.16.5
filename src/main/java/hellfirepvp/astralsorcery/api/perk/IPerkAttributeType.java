package hellfirepvp.astralsorcery.api.perk;

import net.minecraft.resources.ResourceLocation;

/** Stable identity and arithmetic policy of a perk attribute. */
public interface IPerkAttributeType {
    ResourceLocation getRegistryName();
    boolean isMultiplicative();
}
