package hellfirepvp.astralsorcery.api.perk.source;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

/** A source that contributes perk modifiers to a player. */
public interface ModifierSource {
    boolean canApplySource(Player player, LogicalSide side);
    void onRemove(Player player, LogicalSide side);
    void onApply(Player player, LogicalSide side);
    boolean isEqual(ModifierSource other);
    ResourceLocation getProviderName();
    default ResourceLocation getRegistryName() { return null; }
    default ResourceLocation getProviderKey() { return null; }
}
