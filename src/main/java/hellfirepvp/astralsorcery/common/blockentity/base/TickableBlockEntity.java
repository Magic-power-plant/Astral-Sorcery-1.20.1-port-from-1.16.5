/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.base;

import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TickableBlockEntity
 * Created by HellFirePvP
 * Date: 02.08.2016 / 17:34
 */
//1.16.5 declares `implements ITickableTileEntity`; 1.20.1 removed that interface and drives block-entity ticking
//through BlockEntityTicker instances handed out by EntityBlock#getTicker, so each owning block forwards to tick().
public abstract class TickableBlockEntity extends SynchronizedBlockEntity implements RequiresMultiblockBlockEntity {

    private boolean doesSeeSky = false;
    private int lastUpdateTick = -1;

    //ObserverLib is not embedded in this port, so structure validity is sampled periodically and reused between
    //checks. This avoids repeating a full scan when several callers ask in the same tick while still detecting
    //player-built or broken structures within one second.
    private boolean hasMultiblock = false;
    private int lastMultiblockCheckTick = Integer.MIN_VALUE;
    private StructureType lastRequiredStructure;
    private static final int MULTIBLOCK_CHECK_INTERVAL = 20;

    protected int ticksExisted = 0;

    protected TickableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (ticksExisted == 0) {
            onFirstTick();
        }
        ticksExisted++;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return null;
    }

    //Since no-sky worlds count always as "can't see sky" even if it's exposed to the sky
    //Set to true to always count as seeing the sky in no-sky worlds.
    public boolean seesSkyInNoSkyWorlds() {
        return false;
    }

    protected void onFirstTick() {}

    public int getTicksExisted() {
        return ticksExisted;
    }

    public boolean doesSeeSky() {
        //1.16.5: getWorld().isRemote() -> 1.20.1: level.isClientSide. 1.20.1 BlockEntity#level is null between
        //construction and setLevel, which 1.16.5's getWorld() was not in practice, hence the extra null guard.
        if (level != null && level.isClientSide) {
            return this.doesSeeSky;
        }
        if (level == null) return false;

        if (lastUpdateTick == -1 || (ticksExisted - lastUpdateTick) >= 20) {
            lastUpdateTick = ticksExisted;
            boolean prevSky = doesSeeSky;
            boolean newSky = MiscUtils.canSeeSky(level, worldPosition.above(), true, seesSkyInNoSkyWorlds(), doesSeeSky);
            if (prevSky != newSky) {
                notifySkyStateUpdate(prevSky, newSky);
                this.doesSeeSky = newSky;
                this.markForUpdate();
            }
        }
        return doesSeeSky;
    }

    public boolean hasMultiblock() {
        if (level == null) return false;
        if (level.isClientSide) {
            return this.hasMultiblock;
        }

        if (this.getRequiredStructureType() == null) {
            resetMultiblockState();
            return false;
        }

        StructureType struct = this.getRequiredStructureType();
        if (struct != lastRequiredStructure) {
            lastRequiredStructure = struct;
            lastMultiblockCheckTick = Integer.MIN_VALUE;
        }
        if (lastMultiblockCheckTick != Integer.MIN_VALUE && ticksExisted - lastMultiblockCheckTick < MULTIBLOCK_CHECK_INTERVAL) {
            return this.hasMultiblock;
        }
        lastMultiblockCheckTick = ticksExisted;
        boolean prevFound = this.hasMultiblock;
        boolean found = struct.matches(level, worldPosition);
        if (prevFound != found) {
            LogCategory.STRUCTURE_MATCH.info(() ->
                    "Structure match updated: " + this.getClass().getName() + " at " + this.getBlockPos() +
                            " (" + this.hasMultiblock + " -> " + found + ")");
            notifyMultiblockStateUpdate(prevFound, found);
            this.hasMultiblock = found;
            this.markForUpdate();
        }
        return this.hasMultiblock;
    }

    private void resetMultiblockState() {
        lastRequiredStructure = null;
        lastMultiblockCheckTick = Integer.MIN_VALUE;
        if (this.hasMultiblock) {
            this.notifyMultiblockStateUpdate(true, false);
            this.hasMultiblock = false;
            this.markForUpdate();
        }
    }

    protected void notifySkyStateUpdate(boolean doesSeeSkyPrev, boolean doesSeeSkyNow) {}

    protected void notifyMultiblockStateUpdate(boolean hadMultiblockPrev, boolean hasMultiblockNow) {}

    //1.16.5 overrides readCustomNBT here (TileEntityTick.java:147-154), not read(). That matters: in this tree
    //TileEntitySynchronized#onDataPacket (the block-entity update-packet path) only calls readCustomNBT/readNetNBT
    //and never load(), so reading these three fields in load() meant the client never picked up the synced
    //doesSeeSky/hasMultiblock values that writeCustomNBT puts into every update packet - hasMultiblock() returns
    //the cached field verbatim on the client, so it stayed false forever there. Restored to the reference hook.
    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);
        this.ticksExisted = compound.getInt("ticksExisted");
        this.doesSeeSky = compound.getBoolean("doesSeeSky");
        this.hasMultiblock = compound.getBoolean("hasMultiblock");
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);
        compound.putInt("ticksExisted", this.ticksExisted);
        compound.putBoolean("doesSeeSky", this.doesSeeSky);
        compound.putBoolean("hasMultiblock", this.hasMultiblock);
    }
}
