package hellfirepvp.astralsorcery.common.starlight.transmission.base;

import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.util.data.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;

public abstract class SimpleTransmissionReceiver<T extends BlockEntity> implements ITransmissionReceiver {

    private BlockPos thisPos;
    private final Set<BlockPos> sourcesToThis = new HashSet<>();
    private boolean needsTileSync = false;

    public SimpleTransmissionReceiver(BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    @Override
    public void update(Level world) {
        if (this.needsTileSync) {
            T tile = getTileAtPos(world);
            if (tile != null && this.syncTileData(world, tile)) {
                this.needsTileSync = false;
            }
        }
    }

    public final void markForTileSync() {
        this.needsTileSync = true;
    }

    public abstract boolean syncTileData(Level world, T tile);

    public abstract Class<T> getTileClass();

    @Override
    public BlockPos getLocationPos() {
        return thisPos;
    }

    @Override
    public void notifySourceLink(Level world, BlockPos source) {
        sourcesToThis.add(source);
    }

    @Override
    public void notifySourceUnlink(Level world, BlockPos source) {
        sourcesToThis.remove(source);
    }

    @Override
    public boolean notifyBlockChange(Level world, BlockPos changed) {
        return false;
    }

    @Override
    public List<BlockPos> getSources() {
        return new LinkedList<>(sourcesToThis);
    }

    @Nullable
    public T getTileAtPos(Level world) {
        if (world == null || thisPos == null) {
            return null;
        }
        BlockEntity tile = world.getBlockEntity(thisPos);
        if (getTileClass().isInstance(tile)) {
            return getTileClass().cast(tile);
        }
        return null;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        this.sourcesToThis.clear();
        this.thisPos = NBTHelper.readBlockPosFromNBT(compound);
        this.needsTileSync = compound.getBoolean("needsTileSync");

        ListTag sources = compound.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sources.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(sources.getCompound(i)));
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        NBTHelper.writeBlockPosToNBT(thisPos, compound);
        compound.putBoolean("needsTileSync", this.needsTileSync);

        ListTag sources = new ListTag();
        for (BlockPos source : sourcesToThis) {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(source, tag);
            sources.add(tag);
        }
        compound.put("sources", sources);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleTransmissionReceiver<?> that = (SimpleTransmissionReceiver<?>) o;
        return Objects.equals(thisPos, that.thisPos);
    }

    @Override
    public int hashCode() {
        return thisPos != null ? thisPos.hashCode() : 0;
    }
}
