package hellfirepvp.astralsorcery.api.perk;

import hellfirepvp.astralsorcery.common.perk.PerkAttributeMap;
import hellfirepvp.astralsorcery.common.perk.type.ModifierType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

/** Reads the effective value of one perk attribute. */
public interface IPerkAttributeReader {
    IPerkAttributeType getType();
    double getDefaultValue(PerkAttributeMap statMap, Player player, LogicalSide side);
    double getModifierValueForMode(PerkAttributeMap statMap, Player player, LogicalSide side, ModifierType mode);
}
