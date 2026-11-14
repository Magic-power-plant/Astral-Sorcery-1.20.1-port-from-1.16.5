package hellfirepvp.astralsorcery.common.structure.match.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SimpleMatchableBlockEntity<T extends BlockEntity> implements MatchableBlockEntity<T> {

    private final BlockEntityType<T> blockEntityType;
    private final BiConsumer<T, CompoundTag> writeDisplayData;
    private final Consumer<T> writePlacement;

    public SimpleMatchableBlockEntity(BlockEntityType<T> blockEntityType, BiConsumer<T, CompoundTag> writeDisplayData, Consumer<T> writePlacement) {
        this.blockEntityType = blockEntityType;
        this.writeDisplayData = writeDisplayData;
        this.writePlacement = writePlacement;
    }

    @Override
    public void writeDisplayData(@Nonnull T blockEntity, long tick, @Nonnull CompoundTag tag) {
        this.writeDisplayData.accept(blockEntity, tag);
    }

    @Override
    public void postPlacement(@Nonnull T blockEntity, @Nonnull BlockGetter world, BlockPos pos) {
        this.writePlacement.accept(blockEntity);
    }

    @Override
    public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull T blockEntity) {
        return blockEntity.getType().equals(this.blockEntityType);
    }
}
