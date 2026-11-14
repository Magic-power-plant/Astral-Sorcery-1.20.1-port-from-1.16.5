package hellfirepvp.astralsorcery.common.starlight.transmission.base;

import hellfirepvp.astralsorcery.common.starlight.transmission.ITransmissionReceiver;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
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
    private boolean needsBlockEntitySync = false;

    public SimpleTransmissionReceiver(BlockPos thisPos) {
        this.thisPos = thisPos;
    }

    @Override
    public void update(Level world) {
        if (this.needsBlockEntitySync) {
            T blockEntity = getBlockEntityAtPos(world);
            if (blockEntity != null && this.syncBlockEntityData(world, blockEntity)) {
                this.needsBlockEntitySync = false;
            }
        }
    }

    public final void markForBlockEntitySync() {
        this.needsBlockEntitySync = true;
    }

    public abstract boolean syncBlockEntityData(Level world, T blockEntity);

    public abstract Class<T> getBlockEntityClass();

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
    public T getBlockEntityAtPos(Level world) {
        if (world == null || thisPos == null) {
            return null;
        }
        BlockEntity blockEntity = world.getBlockEntity(thisPos);
        if (getBlockEntityClass().isInstance(blockEntity)) {
            return getBlockEntityClass().cast(blockEntity);
        }
        return null;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        this.sourcesToThis.clear();
        this.thisPos = NBTHelper.readBlockPosFromNBT(compound);
        this.needsBlockEntitySync = compound.getBoolean("needsTileSync");

        ListTag sources = compound.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < sources.size(); i++) {
            sourcesToThis.add(NBTHelper.readBlockPosFromNBT(sources.getCompound(i)));
        }
    }

    @Override
    public void writeToNBT(CompoundTag compound) {
        NBTHelper.writeBlockPosToNBT(thisPos, compound);
        compound.putBoolean("needsTileSync", this.needsBlockEntitySync);

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
