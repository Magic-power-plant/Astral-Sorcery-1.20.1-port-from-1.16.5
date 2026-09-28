package hellfirepvp.astralsorcery.common.data.research;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.lib.AdvancementsAS;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncPerkActivity;
import hellfirepvp.astralsorcery.common.network.play.server.PktSyncModifierSource;
import hellfirepvp.astralsorcery.common.perk.PerkEffectHelper;
import hellfirepvp.astralsorcery.common.perk.PerkTree;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import hellfirepvp.astralsorcery.common.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.data.research.PlayerPerkAllocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ResearchManager {
    public static void unsafeForceGiveResearch(Player player, ResearchProgression progression) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return;
        }
        ProgressionTier requiredTier = progression.getRequiredProgress();
        if (!progress.getTierReached().isThisLaterOrEqual(requiredTier)) {
            progress.setTierReached(requiredTier);
        }
        LinkedList<ResearchProgression> toGive = new LinkedList<>();
        toGive.add(progression);
        while (!toGive.isEmpty()) {
            ResearchProgression give = toGive.pop();
            if (progress.forceGainResearch(give)) {
                toGive.addAll(give.getPreConditions());
            }
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
    }

    public static boolean grantResearch(Player player, ResearchProgression progression) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid() || !canGainResearch(progress, progression)) {
            return false;
        }
        boolean changed = progress.forceGainResearch(progression);
        if (changed) {
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            ResearchHelper.savePlayerKnowledge(player);
        }
        return changed;
    }

    public static boolean canGainResearch(PlayerProgress progress, ResearchProgression progression) {
        if (!progress.getTierReached().isThisLaterOrEqual(progression.getRequiredProgress())) {
            return false;
        }
        for (ResearchProgression preCondition : progression.getPreConditions()) {
            if (!progress.hasResearch(preCondition)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasResearch(Player player, ResearchProgression progression) {
        return ResearchHelper.getProgress(player, LogicalSide.SERVER).hasResearch(progression);
    }

    public static boolean grantProgress(Player player, ProgressionTier tier) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        ProgressionTier current = progress.getTierReached();
        if (!current.hasNextTier() || current.next() != tier) {
            return false;
        }
        progress.setTierReached(tier);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean discoverConstellations(Collection<IConstellation> constellations, Player player) {
        boolean changed = false;
        for (IConstellation constellation : constellations) {
            PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            if (!progress.isValid() || progress.hasConstellationDiscovered(constellation)) {
                continue;
            }
            progress.discoverConstellation(constellation.getRegistryName());
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementsAS.DISCOVER_CONSTELLATION.trigger(serverPlayer, constellation);
            }
            ResearchHelper.sendConstellationDiscoveryMessage(player, constellation);
            changed = true;
        }
        if (changed) {
            PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            ResearchHelper.savePlayerKnowledge(player);
        }
        return changed;
    }

    public static boolean discoverConstellation(IConstellation constellation, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasConstellationDiscovered(constellation)) {
            return false;
        }
        progress.discoverConstellation(constellation.getRegistryName());
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementsAS.DISCOVER_CONSTELLATION.trigger(serverPlayer, constellation);
        }
        ResearchHelper.sendConstellationDiscoveryMessage(player, constellation);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean memorizeConstellation(IConstellation constellation, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid() || progress.hasSeenConstellation(constellation)) {
            return false;
        }
        progress.memorizeConstellation(constellation.getRegistryName());
        ResearchHelper.sendConstellationMemorizationMessage(player, progress, constellation);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean updateConstellationPapers(List<IConstellation> papers, Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.setStoredConstellationPapers(papers.stream().map(IConstellation::getRegistryName).collect(Collectors.toList()));
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean maximizeTier(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.setTierReached(ProgressionTier.values()[ProgressionTier.values().length - 1]);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setAttunedBefore(Player player, boolean wasAttunedBefore) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.setAttunedBefore(wasAttunedBefore);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setAttunedConstellation(Player player, @Nullable IMajorConstellation constellation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        if (constellation != null && !progress.hasConstellationDiscovered(constellation)) {
            return false;
        }
        removeAllAllocatedPerks(progress, player);
        progress.getPerkData().setExp(0);
        progress.setAttunedConstellation(constellation);
        if (constellation != null) {
            AbstractPerk root = PerkTree.PERK_TREE.getRootPerk(LogicalSide.SERVER, constellation);
            if (root != null) {
                doApplyPerk(progress, progress.getPerkData(), player, root, PlayerPerkAllocation.unlock());
            }
        }
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementsAS.ATTUNE_SELF.trigger(serverPlayer, constellation);
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean setTomeReceived(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.setTomeReceived();
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean togglePerkAbilities(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.setUsePerkAbilities(!progress.doPerkAbilities());
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    //1.16.5 ResearchManager.java:442-453. Restored because ItemShiftingStar needs it to put a player's perk
    //experience back after re-attuning: setAttunedConstellation deliberately zeroes it (:192) and then persists
    //that with savePlayerKnowledge (:204), so without this the Shifting Star silently destroyed all accumulated
    //perk exp on disk.
    //PlayerPerkData#setExp is protected, but this class sits in the same package, which is why :192 can already
    //call it - the BLOCKED_PORT[AS-ITM-11] note that claimed otherwise was wrong.
    //Deviation from 1.16.5: that version casts unconditionally to ServerPlayerEntity to fire the advancement.
    //Here the trigger is guarded exactly as modifyExp below does, so a non-server Player cannot throw a CCE.
    public static boolean setExp(Player player, long exp) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        progress.getPerkData().setExp(exp);
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementsAS.PERK_LEVEL.trigger(serverPlayer);
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static void modifyExp(Player player, float exp) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (progress.isValid()) {
            progress.getPerkData().modifyExp(exp, player);
            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementsAS.PERK_LEVEL.trigger(serverPlayer);
            }
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            ResearchHelper.savePlayerKnowledge(player);
        }
    }

    public static void setPerkData(Player player, AbstractPerk perk, CompoundTag prev, CompoundTag data) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (progress.isValid()) {
            PlayerPerkData perkData = progress.getPerkData();
            if (!perkData.hasPerkAllocation(perk)) {
                return;
            }

            PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.REMOVE);
            perkData.updatePerkData(perk, data);
            PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);

            if (player instanceof ServerPlayer serverPlayer) {
                PacketChannel.sendToPlayer(serverPlayer, new PktSyncPerkActivity(perk.getRegistryName(), prev, data));
            }
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            ResearchHelper.savePlayerKnowledge(player);
        }
    }

    public static boolean applyPerk(Player player, @Nonnull AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        PlayerPerkData perkData = progress.getPerkData();
        if (allocation.getType() == PerkAllocationType.UNLOCKED
                && !perkData.hasFreeAllocationPoint(player, LogicalSide.SERVER)) {
            return false;
        }
        if (!doApplyPerk(progress, perkData, player, perk, allocation)) {
            return false;
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean forceApplyPerk(Player player, AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (progress.isValid()) {
            if (!doApplyPerk(progress, progress.getPerkData(), player, perk, allocation)) {
                return false;
            }
            ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
            ResearchHelper.savePlayerKnowledge(player);
            return true;
        }
        return false;
    }

    public static boolean resetPerks(Player player) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        removeAllAllocatedPerks(progress, player);
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean applyPerkSeal(Player player, @Nonnull AbstractPerk perk) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.canSealPerk(perk)) {
            return false;
        }
        if (!perkData.sealPerk(perk)) {
            return false;
        }

        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.REMOVE);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketChannel.sendToPlayer(serverPlayer, PktSyncModifierSource.remove(perk));
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean breakPerkSeal(Player player, @Nonnull AbstractPerk perk) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!progress.isValid()) {
            return false;
        }
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.isPerkSealed(perk) || !perkData.breakSeal(perk)) {
            return false;
        }

        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketChannel.sendToPlayer(serverPlayer, PktSyncModifierSource.add(perk));
        }
        ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
        ResearchHelper.savePlayerKnowledge(player);
        return true;
    }

    public static boolean grantFreePerkPoint(Player player, ResourceLocation token) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (progress.isValid()) {
            boolean changed = progress.getPerkData().grantFreeAllocationPoint(token);
            if (changed) {
                ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
                ResearchHelper.savePlayerKnowledge(player);
            }
            return changed;
        }
        return false;
    }

    public static boolean revokeFreePoint(Player player, ResourceLocation token) {
        PlayerProgress progress = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (progress.isValid()) {
            boolean changed = progress.getPerkData().tryRevokeAllocationPoint(token);
            if (changed) {
                ResearchSyncHelper.pushProgressToClientUnsafe(progress, player);
                ResearchHelper.savePlayerKnowledge(player);
            }
            return changed;
        }
        return false;
    }

    public static boolean forceMaximizeAll(Player player) {
        boolean changed = discoverConstellations(ConstellationRegistry.getAllConstellations(), player);
        changed |= maximizeTier(player);
        for (ResearchProgression progression : ResearchProgression.values()) {
            unsafeForceGiveResearch(player, progression);
            changed = true;
        }
        changed |= setAttunedBefore(player, true);
        return changed;
    }

    /**
     * Port of 1.16.5 ResearchManager#informCraftedAltar: research progression for the crafted stack
     * plus the astralsorcery:altar_craft advancement trigger for the crafting player.
     */
    public static void informCraftedAltar(@Nonnull TileAltar altar, @Nonnull ActiveAltarRecipe recipe, @Nonnull ItemStack crafted) {
        ServerPlayer crafter = recipe.tryGetCraftingPlayerServer(altar);
        if (crafter == null) {
            AstralSorcery.LOG.warn("Crafting finished, player that initialized crafting could not be found!");
            AstralSorcery.LOG.warn("Affected tile: " + altar.getBlockPos() + " in dim " + (altar.getLevel() == null ? "<unknown>" : altar.getLevel().dimension().location()));
            return;
        }

        informCrafted(crafter, crafted);

        AdvancementsAS.ALTAR_CRAFT.trigger(crafter, recipe.getRecipe(), crafted);
    }

    public static void informCrafted(@Nonnull Player player, @Nonnull ItemStack crafted) {
        if (!crafted.isEmpty()) {
            informCraftCompletion(player, crafted.getItem(), Block.byItem(crafted.getItem()));
        }
    }

    private static void removeAllAllocatedPerks(PlayerProgress progress, Player player) {
        PlayerPerkData perkData = progress.getPerkData();
        List<AbstractPerk> allocatedPerks = new LinkedList<>(perkData.getAllocatedPerks(PerkAllocationType.UNLOCKED));
        for (AbstractPerk perk : allocatedPerks) {
            doRemovePerk(progress, player, LogicalSide.SERVER, perk, PlayerPerkAllocation.unlock());
        }
    }

    private static boolean doRemovePerk(PlayerProgress progress, Player player, LogicalSide side,
                                        AbstractPerk perk, PlayerPerkAllocation allocation) {
        PlayerPerkData perkData = progress.getPerkData();
        if (!perkData.hasPerkAllocation(perk, allocation.getType())) {
            return false;
        }
        CompoundTag data = perkData.getData(perk);
        if (data == null) {
            return false;
        }
        PerkRemovalResult simulated = perkData.removePerkAllocation(perk, allocation, true);
        if (simulated.isFailure()) {
            return false;
        }
        if (simulated.removesPerk()) {
            PerkEffectHelper.modifySource(player, side, perk, PerkEffectHelper.Action.REMOVE);
        }
        if (simulated.removesAllocationType()) {
            perk.onRemovePerkServer(player, allocation.getType(), progress, data);
        }
        perkData.removePerkAllocation(perk, allocation, false);
        return true;
    }

    private static boolean doApplyPerk(PlayerProgress progress, PlayerPerkData perkData, Player player,
                                       AbstractPerk perk, PlayerPerkAllocation allocation) {
        if (!perkData.applyPerkAllocation(perk, allocation, true)) {
            return false;
        }
        if (perkData.hasPerkAllocation(perk)) {
            if (!perkData.hasPerkAllocation(perk, allocation.getType())) {
                CompoundTag data = perkData.getData(perk);
                if (data == null) {
                    return false;
                }
                perk.onUnlockPerkServer(player, allocation.getType(), progress, data);
                perkData.updatePerkData(perk, data);
            }
            return perkData.applyPerkAllocation(perk, allocation, false);
        }

        CompoundTag data = new CompoundTag();
        perk.onUnlockPerkServer(player, allocation.getType(), progress, data);
        if (!perkData.applyPerkAllocation(perk, allocation, false)) {
            return false;
        }
        perkData.updatePerkData(perk, data);
        PerkEffectHelper.modifySource(player, LogicalSide.SERVER, perk, PerkEffectHelper.Action.ADD);
        return true;
    }

    private static void informCraftCompletion(@Nonnull Player player, @Nonnull Item itemCrafted, @Nonnull Block blockCrafted) {
        if (blockCrafted == BlocksAS.ALTAR_DISCOVERY.get()) {
            grantProgress(player, ProgressionTier.BASIC_CRAFT);
            grantResearch(player, ResearchProgression.BASIC_CRAFT);
        } else if (blockCrafted == BlocksAS.ALTAR_ATTUNEMENT.get()) {
            grantAltarProgress(player, AltarType.ATTUNEMENT);
        } else if (blockCrafted == BlocksAS.ALTAR_CONSTELLATION.get()) {
            grantAltarProgress(player, AltarType.CONSTELLATION);
        } else if (blockCrafted == BlocksAS.ALTAR_RADIANCE.get()) {
            grantAltarProgress(player, AltarType.RADIANCE);
        }
    }

    private static void grantAltarProgress(Player player, AltarType craftedType) {
        grantProgress(player, ProgressionTier.BASIC_CRAFT);
        grantResearch(player, ResearchProgression.BASIC_CRAFT);
        if (craftedType.isThisGEThan(AltarType.ATTUNEMENT)) {
            grantProgress(player, ProgressionTier.ATTUNEMENT);
            grantResearch(player, ResearchProgression.ATTUNEMENT);
        }
        if (craftedType.isThisGEThan(AltarType.CONSTELLATION)) {
            grantProgress(player, ProgressionTier.CONSTELLATION_CRAFT);
            grantResearch(player, ResearchProgression.CONSTELLATION);
        }
        if (craftedType.isThisGEThan(AltarType.RADIANCE)) {
            grantProgress(player, ProgressionTier.TRAIT_CRAFT);
            grantResearch(player, ResearchProgression.RADIANCE);
        }
    }
}
