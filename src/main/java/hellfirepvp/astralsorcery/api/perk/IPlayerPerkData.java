package hellfirepvp.astralsorcery.api.perk;

import hellfirepvp.astralsorcery.common.data.research.PerkAllocationType;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

import java.util.Collection;
import java.util.function.Predicate;

/** Read-only query surface for a player's perk allocations. */
public interface IPlayerPerkData {
    Collection<AbstractPerk> getSealedPerks();
    Collection<AbstractPerk> getEffectGrantingPerks();
    Collection<AbstractPerk> getAllocatedPerks(PerkAllocationType type);
    Collection<PerkAllocationType> getAllocationTypes(AbstractPerk perk);
    boolean hasPerkEffect(Predicate<AbstractPerk> perkMatch);
    boolean hasPerkEffect(AbstractPerk perk);
    boolean hasPerkAllocation(Predicate<AbstractPerk> perkMatch);
    boolean hasPerkAllocation(AbstractPerk perk);
    boolean hasPerkAllocation(AbstractPerk perk, PerkAllocationType type);
    boolean isPerkSealed(AbstractPerk perk);
    boolean isPerkSealed(Predicate<AbstractPerk> perkMatch);
    Collection<ResourceLocation> getFreePointTokens();
    int getAvailablePerkPoints(Player player, LogicalSide side);
    boolean hasFreeAllocationPoint(Player player, LogicalSide side);
    double getPerkExp();
    int getPerkLevel(Player player, LogicalSide side);
    float getPercentToNextLevel(Player player, LogicalSide side);
}
