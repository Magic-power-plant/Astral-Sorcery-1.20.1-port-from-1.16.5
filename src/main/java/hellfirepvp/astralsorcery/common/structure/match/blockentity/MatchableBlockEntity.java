package hellfirepvp.astralsorcery.common.structure.match.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MatchableBlockEntity<T extends BlockEntity> {

    void writeDisplayData(@Nonnull T blockEntity, long tick, @Nonnull CompoundTag tag);

    void postPlacement(@Nonnull T blockEntity, @Nonnull BlockGetter world, BlockPos pos);

    boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull T blockEntity);
}
