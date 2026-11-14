package hellfirepvp.astralsorcery.common.structure.match.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MatchableState {

    MatchableState AIR = new MatchableState() {
        @Nonnull
        @Override
        public BlockState getDescriptiveState(long tick) {
            return Blocks.AIR.defaultBlockState();
        }

        @Override
        public boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state) {
            return state.isAir();
        }
    };

    MatchableState REQUIRES_AIR = AIR;

    @Nonnull
    BlockState getDescriptiveState(long tick);

    @Nullable
    default BlockEntity createBlockEntity(BlockGetter blockReader, BlockPos at, long tick) {
        BlockState state = getDescriptiveState(tick);
        if (state.getBlock() instanceof EntityBlock entityBlock) {
            return entityBlock.newBlockEntity(at, state);
        }
        return null;
    }

    boolean matches(@Nullable BlockGetter reader, @Nonnull BlockPos absolutePosition, @Nonnull BlockState state);
}
