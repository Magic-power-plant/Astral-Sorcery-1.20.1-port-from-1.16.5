/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.event.EventFlags;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockPredicate;
import hellfirepvp.astralsorcery.common.block.util.BlockUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeMiningSize
 * Created by HellFirePvP
 * Date: 29.03.2020 / 15:50
 */
public class AttributeTypeMiningSize extends PerkAttributeType {

    public static final Config CONFIG = new Config("type." + PerkAttributeTypesAS.KEY_ATTR_TYPE_MINING_SIZE.getPath());

    public AttributeTypeMiningSize() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_MINING_SIZE);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);

        eventBus.addListener(this::onBreak);
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        Player player = event.getPlayer();

        if (level.isClientSide()) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
            if (!prog.doPerkAbilities() || MiscUtils.isPlayerFakeMP(serverPlayer)) {
                return;
            }
            EventFlags.MINING_SIZE_BREAK.executeWithFlag(() -> {
                float size = PerkAttributeHelper.getOrCreateMap(player, LogicalSide.SERVER)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_MINING_SIZE, 0);
                size = AttributeEvent.postProcessModded(player, PerkAttributeTypesAS.ATTR_TYPE_MINING_SIZE, size);
                if (size >= 1F) {
                    BlockHitResult brtr = MiscUtils.rayTraceLookBlock(player, net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE);
                    if (brtr != null && brtr.getType() == HitResult.Type.BLOCK) {
                        float hardnessBroken = event.getState().getDestroySpeed(level, event.getPos());
                        BlockPredicate miningTest = (worldIn, posIn, stateIn) ->
                                stateIn.getDestroySpeed(worldIn, posIn) >= 0 &&
                                        stateIn.getDestroySpeed(worldIn, posIn) <= hardnessBroken;
                        Direction dir = brtr.getDirection();
                        if (dir.getAxis() == Direction.Axis.Y) {
                            this.breakBlocksPlaneHorizontal(serverPlayer, dir, level, event.getPos(), miningTest, net.minecraft.util.Mth.floor(size));
                        } else {
                            this.breakBlocksPlaneVertical(serverPlayer, dir, level, event.getPos(), miningTest, net.minecraft.util.Mth.floor(size));
                        }
                    }
                }
            });
        }
    }

    private void breakBlocksPlaneVertical(ServerPlayer player, Direction sideBroken, Level level, BlockPos at, BlockPredicate miningTest, int size) {
        if (size <= 0) {
            return;
        }
        for (int xx = -size; xx <= size; xx++) {
            if (sideBroken.getNormal().getX() != 0 && xx != 0) continue;
            for (int yy = -1; yy <= (size * 2 - 1); yy++) {
                if (sideBroken.getNormal().getY() != 0 && yy != 0) continue;
                for (int zz = -size; zz <= size; zz++) {
                    if (sideBroken.getNormal().getZ() != 0 && zz != 0) continue;
                    if (xx == 0 && yy == 0 && zz == 0) continue;

                    BlockPos other = at.offset(xx, yy, zz);
                    BlockState otherState = level.getBlockState(other);
                    if (otherState.getDestroySpeed(level, other) != -1 &&
                            (player.isCreative() || miningTest.test(level, other, otherState)) &&
                            AlignmentChargeHandler.INSTANCE.drainCharge(player, CONFIG.chargeCostPerBreak.get(), true)) {
                        BlockState state = level.getBlockState(other);
                        if (!BlockUtils.isFluidBlock(state) &&
                                (player.isCreative() || otherState.canHarvestBlock(level, other, player)) &&
                                player.gameMode.destroyBlock(other)) {
                            if (rand.nextInt(3) == 0) {
                                AlignmentChargeHandler.INSTANCE.drainCharge(player, CONFIG.chargeCostPerBreak.get(), false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void breakBlocksPlaneHorizontal(ServerPlayer player, Direction sideBroken, Level level, BlockPos at, BlockPredicate miningTest, int size) {
        if (size <= 0) {
            return;
        }
        for (int xx = -size; xx <= size; xx++) {
            if (sideBroken.getNormal().getX() != 0 && xx != 0) continue;
            for (int zz = -size; zz <= size; zz++) {
                if (sideBroken.getNormal().getZ() != 0 && zz != 0) continue;
                if (xx == 0 && zz == 0) continue;

                BlockPos other = at.offset(xx, 0, zz);
                BlockState otherState = level.getBlockState(other);
                if (otherState.getDestroySpeed(level, other) != -1 &&
                        (player.isCreative() || miningTest.test(level, other, otherState)) &&
                        AlignmentChargeHandler.INSTANCE.drainCharge(player, CONFIG.chargeCostPerBreak.get(), true)) {
                    BlockState state = level.getBlockState(other);
                    if (!BlockUtils.isFluidBlock(state) &&
                            (player.isCreative() || otherState.canHarvestBlock(level, other, player)) &&
                            player.gameMode.destroyBlock(other)) {
                        if (rand.nextInt(3) == 0) {
                            AlignmentChargeHandler.INSTANCE.drainCharge(player, CONFIG.chargeCostPerBreak.get(), false);
                        }
                    }
                }
            }
        }
    }

    private static class Config extends ConfigEntry {

        private ForgeConfigSpec.IntValue chargeCostPerBreak;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ForgeConfigSpec.Builder cfgBuilder) {
            chargeCostPerBreak = cfgBuilder
                    .comment("Defines the amount of starlight charge consumed per additional block break through this attribute.")
                    .translation(translationKey("chargeCostPerBreak"))
                    .defineInRange("chargeCostPerBreak", 2, 1, 500);
        }
    }
}

