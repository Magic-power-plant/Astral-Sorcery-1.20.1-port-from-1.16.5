/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.root;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import hellfirepvp.astralsorcery.common.perk.tick.PlayerTickPerk;
import hellfirepvp.astralsorcery.common.util.math.DiminishingMultiplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RootVicio
 * Created by HellFirePvP
 * Date: 01.09.2019 / 10:34
 */
public class RootVicio extends RootPerk implements PlayerTickPerk {

    public static final Config CONFIG = new Config("root.vicio");

    private final Map<ResourceLocation, Map<UUID, Integer>> moveTrackMap = new HashMap<>();

    public RootVicio(ResourceLocation name, float x, float y) {
        super(name, CONFIG, ConstellationsAS.vicio, x, y);
    }

    @Nonnull
    @Override
    protected DiminishingMultiplier createMultiplier() {
        return new DiminishingMultiplier(10_000L, 0.065F, 0.003F, 0.2F);
    }

    @Override
    public void removePerkLogic(Player player, LogicalSide side) {
        super.removePerkLogic(player, side);

        if (side.isServer()) {
            this.moveTrackMap.computeIfAbsent(Stats.WALK_ONE_CM, s -> new HashMap<>()).remove(player.getUUID());
            this.moveTrackMap.computeIfAbsent(Stats.SPRINT_ONE_CM, s -> new HashMap<>()).remove(player.getUUID());
            this.moveTrackMap.computeIfAbsent(Stats.FLY_ONE_CM, s -> new HashMap<>()).remove(player.getUUID());
            this.moveTrackMap.computeIfAbsent(Stats.AVIATE_ONE_CM, s -> new HashMap<>()).remove(player.getUUID());
            this.moveTrackMap.computeIfAbsent(Stats.SWIM_ONE_CM, s -> new HashMap<>()).remove(player.getUUID());
        }
    }

    @Override
    public void clearCaches(LogicalSide side) {
        super.clearCaches(side);

        if (side.isServer()) {
            this.moveTrackMap.clear();
        }
    }

    @Override
    public void onPlayerTick(Player player, LogicalSide side) {
        if (!side.isServer() || !(player instanceof ServerPlayer)) {
            return;
        }

        UUID uuid = player.getUUID();
        ServerPlayer sPlayer = (ServerPlayer) player;
        PlayerProgress prog = ResearchHelper.getProgress(player, side);

        net.minecraft.stats.StatsCounter mgr = sPlayer.getStats();
        net.minecraft.stats.StatType<net.minecraft.resources.ResourceLocation> customStats = net.minecraft.stats.Stats.CUSTOM;
        int walked = mgr.getValue(customStats, net.minecraft.stats.Stats.WALK_ONE_CM);
        int sprint = mgr.getValue(customStats, net.minecraft.stats.Stats.SPRINT_ONE_CM);
        int flown = mgr.getValue(customStats, net.minecraft.stats.Stats.FLY_ONE_CM);
        int elytra = mgr.getValue(customStats, net.minecraft.stats.Stats.AVIATE_ONE_CM);
        int swam = mgr.getValue(customStats, net.minecraft.stats.Stats.SWIM_ONE_CM);

        int lastWalked = this.moveTrackMap.computeIfAbsent(net.minecraft.stats.Stats.WALK_ONE_CM, s -> new HashMap<>()).computeIfAbsent(uuid, u -> walked);
        int lastSprint = this.moveTrackMap.computeIfAbsent(net.minecraft.stats.Stats.SPRINT_ONE_CM, s -> new HashMap<>()).computeIfAbsent(uuid, u -> sprint);
        int lastFly = this.moveTrackMap.computeIfAbsent(net.minecraft.stats.Stats.FLY_ONE_CM, s -> new HashMap<>()).computeIfAbsent(uuid, u -> flown);
        int lastElytra = this.moveTrackMap.computeIfAbsent(net.minecraft.stats.Stats.AVIATE_ONE_CM, s -> new HashMap<>()).computeIfAbsent(uuid, u -> elytra);
        int lastSwam = this.moveTrackMap.computeIfAbsent(net.minecraft.stats.Stats.SWIM_ONE_CM, s -> new HashMap<>()).computeIfAbsent(uuid, u -> swam);

        float added = 0;

        if (walked > lastWalked) {
            added += Math.min(walked - lastWalked, 500F);
            if (added >= 500F) {
                added = 500F;
            }
            added *= 0.9F;
            this.moveTrackMap.get(net.minecraft.stats.Stats.WALK_ONE_CM).put(uuid, walked);
        }
        if (sprint > lastSprint) {
            added += Math.min(sprint - lastSprint, 500F);
            if (added >= 500F) {
                added = 500F;
            }
            added *= 0.9F;
            this.moveTrackMap.get(net.minecraft.stats.Stats.SPRINT_ONE_CM).put(uuid, sprint);
        }
        if (flown > lastFly) {
            added += Math.min(flown - lastFly, 500F);
            if (added >= 500F) {
                added = 500F;
            }
            added *= 0.9F;
            this.moveTrackMap.get(net.minecraft.stats.Stats.FLY_ONE_CM).put(uuid, flown);
        }
        if (elytra > lastElytra) {
            added += Math.min(elytra - lastElytra, 500F);
            if (added >= 500F) {
                added = 500F;
            }
            added *= 0.9F;
            this.moveTrackMap.get(net.minecraft.stats.Stats.AVIATE_ONE_CM).put(uuid, elytra);
        }
        if (swam > lastSwam) {
            added += Math.min(swam - lastSwam, 500F);
            if (added >= 500F) {
                added = 500F;
            }
            added *= 0.9F;
            this.moveTrackMap.get(net.minecraft.stats.Stats.SWIM_ONE_CM).put(uuid, swam);
        }

        added = Math.min(added, 70F);
        added *= this.getExpMultiplier();
        added *= this.getDiminishingReturns(player);
        added *= PerkAttributeHelper.getOrCreateMap(player, side).getModifier(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT);
        added *= PerkAttributeHelper.getOrCreateMap(player, side).getModifier(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EXP);
        added = AttributeEvent.postProcessModded(player, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EXP, added);

        if (added > 0) {
            ResearchManager.modifyExp(player, added);
        }
    }
}

