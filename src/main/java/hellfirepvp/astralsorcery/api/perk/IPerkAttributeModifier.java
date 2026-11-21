package hellfirepvp.astralsorcery.api.perk;

import hellfirepvp.astralsorcery.api.progression.IPlayerProgression;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/** Read-only view of one applied perk attribute modifier. */
public interface IPerkAttributeModifier {
    ResourceLocation getComparisonKey();
    IPerkAttributeType getAttributeType();
    ModifierType getMode();
    float getValue(Player player, IPlayerProgression progression);
}
