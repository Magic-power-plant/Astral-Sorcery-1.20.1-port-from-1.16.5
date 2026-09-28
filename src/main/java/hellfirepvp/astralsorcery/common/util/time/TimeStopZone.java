package hellfirepvp.astralsorcery.common.util.time;

import hellfirepvp.astralsorcery.common.data.config.registry.TileAccelerationBlacklistRegistry;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TimeStopZone {

    final EntityTargetController targetController;

    final float range;
    final BlockPos offset;
    private final Level world;
    private int ticksToLive;

    private boolean active = true;

    private final List<BlockEntity> cachedTiles = new LinkedList<>();

    TimeStopZone(EntityTargetController ctrl, float range, BlockPos offset, Level world, int tickLivespan) {
        this.targetController = ctrl;
        this.range = range;
        this.offset = offset;
        this.world = world;
        this.ticksToLive = tickLivespan;
    }

    void onServerTick() {
        if (!active) return;
        this.ticksToLive--;

        int minX = Mth.floor((offset.getX() - range) / 16.0D);
        int maxX = Mth.floor((offset.getX() + range) / 16.0D);
        int minZ = Mth.floor((offset.getZ() - range) / 16.0D);
        int maxZ = Mth.floor((offset.getZ() + range) / 16.0D);

        for (int xx = minX; xx <= maxX; ++xx) {
            for (int zz = minZ; zz <= maxZ; ++zz) {
                LevelChunk ch = world.getChunk(xx, zz);
                if (!ch.isEmpty()) {
                    Map<BlockPos, BlockEntity> map = ch.getBlockEntities();
                    for (Map.Entry<BlockPos, BlockEntity> teEntry : map.entrySet()) {
                        BlockEntity te = teEntry.getValue();
                        if (TileAccelerationBlacklistRegistry.INSTANCE.canBeInfluenced(te) &&
                                te.getBlockPos().distSqr(offset) <= range * range) {
                            safeCacheTile(te);
                            te.setRemoved();
                        }
                    }
                }
            }
        }
    }

    private void safeCacheTile(BlockEntity te) {
        if (te == null) return;

        for (BlockEntity tile : cachedTiles) {
            if (tile.getBlockPos().equals(te.getBlockPos())) {
                return;
            }
        }
        cachedTiles.add(te);
    }

    public void setTicksToLive(int ticksToLive) {
        this.ticksToLive = ticksToLive;
    }

    void stopEffect() {
        for (BlockEntity cached : cachedTiles) {
            BlockState state = world.getBlockState(cached.getBlockPos());
            if (state.hasBlockEntity()) {
                BlockEntity te = world.getBlockEntity(cached.getBlockPos());
                if (te != null && te.getClass().isAssignableFrom(cached.getClass())) {
                    world.getBlockEntity(cached.getBlockPos()); // re-validate
                }
            }
        }
        this.cachedTiles.clear();
        this.active = false;
    }

    boolean shouldDespawn() {
        return ticksToLive <= 0 || !active;
    }

    boolean interceptEntityTick(LivingEntity e) {
        return active && e != null && targetController.shouldFreezeEntity(e) && Vector3.atEntityCorner(e).distance(offset) <= range;
    }

    static void handleImportantEntityTicks(LivingEntity e) {
        if (e.hurtTime > 0) {
            e.hurtTime--;
        }
        if (e.invulnerableTime > 0) {
            e.invulnerableTime--;
        }
        e.xOld = e.getX();
        e.yOld = e.getY();
        e.zOld = e.getZ();
        e.yRotO = e.getYRot();
        e.xRotO = e.getXRot();
        e.yHeadRotO = e.yHeadRot;
        e.walkDistO = e.walkDist;

        if (!e.level().isClientSide()) {
            e.travel(Vec3.ZERO);
        }

        if (e instanceof EnderDragon) {
            DragonPhaseInstance phase = ((EnderDragon) e).getPhaseManager().getCurrentPhase();
            if (phase.getPhase() != EnderDragonPhase.HOLDING_PATTERN &&
                    phase.getPhase() != EnderDragonPhase.DYING) {
                ((EnderDragon) e).getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
            }
        }
    }

    public static class EntityTargetController {

        final int ownerId;
        final boolean hasOwner;
        final boolean targetPlayers;

        EntityTargetController(int ownerId, boolean hasOwner, boolean targetPlayers) {
            this.ownerId = ownerId;
            this.hasOwner = hasOwner;
            this.targetPlayers = targetPlayers;
        }

        boolean shouldFreezeEntity(LivingEntity e) {
            if (!e.isAlive() || e.getHealth() <= 0) {
                return false;
            }
            if (e instanceof EnderDragon && ((EnderDragon) e).getPhaseManager().getCurrentPhase() == EnderDragonPhase.DYING) {
                return false;
            }
            if (hasOwner && e.getId() == ownerId) {
                return false;
            }
            return targetPlayers || !(e instanceof Player);
        }

        public static EntityTargetController allExcept(Entity entity) {
            return new EntityTargetController(entity.getId(), true, true);
        }

        public static EntityTargetController noPlayers() {
            return new EntityTargetController(-1, false, false);
        }

        @Nonnull
        public CompoundTag serializeNBT() {
            CompoundTag out = new CompoundTag();
            out.putBoolean("targetPlayers", this.targetPlayers);
            out.putBoolean("hasOwner", this.hasOwner);
            out.putInt("ownerEntityId", this.ownerId);
            return out;
        }

        @Nonnull
        public static EntityTargetController deserializeNBT(CompoundTag cmp) {
            boolean targetPlayers = cmp.getBoolean("targetPlayers");
            boolean hasOwner = cmp.getBoolean("hasOwner");
            int ownerId = cmp.getInt("ownerEntityId");
            return new EntityTargetController(ownerId, hasOwner, targetPlayers);
        }

    }

}
