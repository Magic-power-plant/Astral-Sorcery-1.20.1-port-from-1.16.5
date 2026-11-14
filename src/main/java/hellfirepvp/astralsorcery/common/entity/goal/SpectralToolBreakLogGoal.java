package hellfirepvp.astralsorcery.common.entity.goal;

import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectPelotrio;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.util.BlockDropCaptureAssist;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.block.BlockPredicate;
import hellfirepvp.astralsorcery.common.util.block.BlockUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpectralToolBreakLogGoal extends SpectralToolGoal {

    private BlockPos selectedBreakPos = null;

    public SpectralToolBreakLogGoal(EntitySpectralTool entity, double speed) {
        super(entity, speed);
    }

    private BlockPredicate breakableLogs() {
        return (world, pos, state) -> {
            return MiscUtils.getBlockEntityAt(world, pos, BlockEntity.class, false) == null &&
                    pos.getY() >= this.getEntity().getStartPosition().getY() &&
                    !state.isAir() &&
                    state.getDestroySpeed(world, pos) != -1 &&
                    state.getDestroySpeed(world, pos) <= 10 &&
                    (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) &&
                    BlockUtils.canToolBreakBlockWithoutPlayer(world, pos, state, new ItemStack(Items.DIAMOND_AXE));
        };
    }

    @Override
    public boolean canUse() {
        MoveControl ctrl = this.getEntity().getMoveControl();

        if (!ctrl.hasWanted()) {
            return true;
        } else {
            BlockPos validPos = BlockDiscoverer.searchAreaForFirst(
                    this.getEntity().level(),
                    this.getEntity().getStartPosition(),
                    8,
                    Vector3.atEntityCorner(this.getEntity()),
                    this.breakableLogs());
            return validPos != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.selectedBreakPos != null;
    }

    @Override
    public void start() {
        super.start();

        BlockPos validPos = BlockDiscoverer.searchAreaForFirst(
                this.getEntity().level(),
                this.getEntity().getStartPosition(),
                10,
                Vector3.atEntityCorner(this.getEntity()),
                this.breakableLogs());

        if (validPos != null) {
            this.selectedBreakPos = validPos;

            this.getEntity().getMoveControl().setWantedPosition(
                    this.selectedBreakPos.getX() + 0.5,
                    this.selectedBreakPos.getY() + 0.5,
                    this.selectedBreakPos.getZ() + 0.5,
                    this.getSpeed());
        }
    }

    @Override
    public void stop() {
        super.stop();

        this.selectedBreakPos = null;
        this.actionCooldown = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!canContinueToUse()) {
            return;
        }

        if (this.actionCooldown < 0) {
            this.actionCooldown = 0;
        }

        Level world = this.getEntity().level();
        boolean resetTimer = false;

        if (world.isEmptyBlock(this.selectedBreakPos)) {
            this.selectedBreakPos = null;
            resetTimer = true;
        } else {
            this.getEntity().getMoveControl().setWantedPosition(
                    this.selectedBreakPos.getX() + 0.5,
                    this.selectedBreakPos.getY() + 0.5,
                    this.selectedBreakPos.getZ() + 0.5,
                    this.getSpeed());

            if (Vector3.atEntityCorner(this.getEntity()).distanceSquared(this.selectedBreakPos) <= 9) {
                this.actionCooldown++;
                if (this.actionCooldown >= MantleEffectPelotrio.CONFIG.ticksPerAxeLogBreak.get() && world instanceof ServerLevel) {
                    LivingEntity owner = this.getEntity().getOwningEntity();
                    if (owner instanceof Player) {
                        BlockDropCaptureAssist.startCapturing();
                    }
                    if (BlockUtils.breakBlockWithoutPlayer(
                            (ServerLevel) world,
                            this.selectedBreakPos,
                            world.getBlockState(this.selectedBreakPos),
                            this.getEntity().getItem(),
                            true,
                            true,
                            true)) {
                        resetTimer = true;
                    }
                    if (owner instanceof Player) {
                        for (ItemStack dropped : BlockDropCaptureAssist.getCapturedStacksAndStop()) {
                            ItemStack remainder = ItemUtils.dropItemToPlayer((Player) owner, dropped);
                            if (!remainder.isEmpty()) {
                                ItemUtils.dropItemNaturally(world, owner.getX(), owner.getY(), owner.getZ(), remainder);
                            }
                        }
                    }
                }
            }
        }

        if (resetTimer) {
            this.actionCooldown = 0;
        }
    }
}
