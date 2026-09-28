/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.base;

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
 * Class: TileEntityTick
 * Created by HellFirePvP
 * Date: 02.08.2016 / 17:34
 */
//1.16.5 declares `implements ITickableTileEntity`; 1.20.1 removed that interface and drives block-entity ticking
//through BlockEntityTicker instances handed out by EntityBlock#getTicker, so each owning block forwards to tick().
public abstract class TileEntityTick extends TileEntitySynchronized implements TileRequiresMultiblock {

    private boolean doesSeeSky = false;
    private int lastUpdateTick = -1;

    //TODO_PORT[AS-TILE-03]: 1.16.5 caches the structure match in an ObserverLib
    //`ChangeSubscriber<ChangeObserverStructure> structureMatch` obtained from `StructureType#observe(world, pos)` and
    //only asks it `isValid(world)` (1.16.5 TileEntityTick.java:36, 99-103), with `refreshMatcher()` dropping the
    //subscriber through ObserverHelper whenever the required structure type changes (1.16.5 lines 118-130).
    //ObserverLib is a source-only reference here and neither ChangeSubscriber nor ChangeObserverStructure is embedded
    //under hellfirepvp.astralsorcery yet, so hasMultiblock() re-runs the full StructureType#matches every call
    //instead of consulting an incrementally-updated subscriber. Same answer, more work per call; refreshMatcher()
    //has no observable effect without the subscriber and is therefore absent.
    private boolean hasMultiblock = false;

    protected int ticksExisted = 0;

    protected TileEntityTick(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
