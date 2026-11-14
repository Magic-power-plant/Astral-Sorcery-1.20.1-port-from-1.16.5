package hellfirepvp.astralsorcery.common.structure.match.structure;

import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.blockentity.MatchableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.TickPriority;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface PlaceableStructure extends Structure {

    default Map<BlockPos, BlockState> placeInWorld(LevelAccessor world, BlockPos center, Predicate<BlockPos> posFilter) {
        Map<BlockPos, BlockState> result = new HashMap<>();
        for (Map.Entry<BlockPos, ? extends MatchableState> entry : getContents().entrySet()) {
            BlockPos at = center.offset(entry.getKey());
            if (!posFilter.test(at)) {
                continue;
            }

            BlockState state = entry.getValue().getDescriptiveState(0);
            BlockState existing = world.getBlockState(at);
            if (!existing.getFluidState().isEmpty() && existing.getFluidState().is(FluidTags.WATER) && state.hasProperty(BlockStateProperties.WATERLOGGED)) {
                state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (!world.setBlock(at, state, Block.UPDATE_ALL)) {
                continue;
            }
            result.put(at, state);

            if (!state.getFluidState().isEmpty()) {
                Fluid fluid = state.getFluidState().getType();
                world.scheduleTick(at, fluid, fluid.getTickDelay(world), TickPriority.HIGH);
            }

            BlockEntity placed = world.getBlockEntity(at);
            if (placed != null && hasBlockEntityAt(entry.getKey())) {
                MatchableBlockEntity blockEntity = getBlockEntityAt(entry.getKey());
                if (blockEntity != null) {
                    blockEntity.postPlacement(placed, world, entry.getKey());
                }
            }
        }
        return result;
    }

    default Map<BlockPos, BlockState> placeInWorld(LevelAccessor world, BlockPos center, Predicate<BlockPos> posFilter, PostPlaceProcessor processor) {
        Map<BlockPos, BlockState> result = placeInWorld(world, center, posFilter);
        if (processor != null) {
            for (Map.Entry<BlockPos, BlockState> entry : result.entrySet()) {
                if (posFilter.test(entry.getKey())) {
                    processor.process(world, entry.getKey(), entry.getValue());
                }
            }
        }
        return result;
    }

    interface PostPlaceProcessor {
        void process(LevelAccessor world, BlockPos pos, BlockState currentState);
    }
}
