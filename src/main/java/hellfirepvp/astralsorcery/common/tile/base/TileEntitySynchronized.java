package hellfirepvp.astralsorcery.common.tile.base;

import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class TileEntitySynchronized extends BlockEntity implements ILocatable, hellfirepvp.astralsorcery.api.sync.SyncTarget {

    protected static final Random rand = new Random();
    //1.16.5: protected static final AxisAlignedBB BOX = new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    protected static final AABB BOX = new AABB(0, 0, 0, 1, 1, 1);
    protected boolean isRemote = false;
    private boolean loadingFromNbt = false;

    public TileEntitySynchronized(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public BlockPos getLocationPos() {
        return this.getBlockPos();
    }

    @Override
    public void markForUpdate() {
        setChanged();
        if (!loadingFromNbt && level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public Level getLevel() {
        return this.level;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        loadingFromNbt = true;
        try {
            readCustomNBT(compound);
            readSaveNBT(compound);
        } finally {
            loadingFromNbt = false;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        writeCustomNBT(compound);
        writeSaveNBT(compound);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        CompoundTag compound = saveWithoutMetadata();
        writeCustomNBT(compound);
        writeNetNBT(compound);
        return ClientboundBlockEntityDataPacket.create(this, (be) -> compound);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            readCustomNBT(tag);
            readNetNBT(tag);
            onDataReceived();
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = super.getUpdateTag();
        writeCustomNBT(compound);
        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    protected void readCustomNBT(CompoundTag compound) {}
    protected void readNetNBT(CompoundTag compound) {}
    protected void readSaveNBT(CompoundTag compound) {}
    protected void writeCustomNBT(CompoundTag compound) {}
    protected void writeNetNBT(CompoundTag compound) {}
    protected void writeSaveNBT(CompoundTag compound) {}

    @OnlyIn(Dist.CLIENT)
    protected void onDataReceived() {}

    @Nullable
    public ItemEntity dropItemOnTop(ItemStack stack) {
        if (level == null) return null;
        return ItemUtils.dropItem(level, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1.5, getBlockPos().getZ() + 0.5, stack);
    }

    public boolean removeSelf() {
        if (level == null || level.isClientSide) {
            return false;
        }
        return level.setBlockAndUpdate(getBlockPos(), Blocks.AIR.defaultBlockState());
    }
}