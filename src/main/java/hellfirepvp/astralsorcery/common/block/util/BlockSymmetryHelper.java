package hellfirepvp.astralsorcery.common.block.util;

import hellfirepvp.astralsorcery.common.util.data.BiDiPair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class BlockSymmetryHelper {

    public SymmetryResult getDotSymmetry(BlockGetter world, BlockPos center, int radiusLayer, boolean allowMirrorSymmetry, Predicate<BlockState> applicableStateFilter) {
        java.util.List<BlockPos> layerPositions = BlockGeometry.getHollowSphere(radiusLayer + 1, radiusLayer);
        SymmetryResult result = new SymmetryResult(layerPositions.size());
        Set<BlockPos> visitedBlocks = new HashSet<>();

        for (BlockPos offset : layerPositions) {
            BlockPos at = center.offset(offset.getX(), offset.getY(), offset.getZ());
            if (visitedBlocks.contains(at)) {
                continue;
            }
            visitedBlocks.add(at);

            BlockState state = world.getBlockState(at);
            if (offset.getX() == 0 || offset.getY() == 0 || offset.getZ() == 0) {
                if (!state.isAir()) {
                    result.fillerBlocks.add(at);
                }
                continue;
            }

            if (applicableStateFilter.test(state)) {
                BlockPos dotSym = center.subtract(offset);
                BlockState dotState = world.getBlockState(dotSym);
                if (applicableStateFilter.test(dotState)) {
                    result.symmetryPairs.add(new BiDiPair<>(at, dotSym));

                    if (!allowMirrorSymmetry) {
                        checkMirrorSymmetry(world, new Vec3i(-offset.getX(),  offset.getY(),  offset.getZ()), center, result, visitedBlocks);
                        checkMirrorSymmetry(world, new Vec3i( offset.getX(), -offset.getY(),  offset.getZ()), center, result, visitedBlocks);
                        checkMirrorSymmetry(world, new Vec3i( offset.getX(),  offset.getY(), -offset.getZ()), center, result, visitedBlocks);
                    }
                } else if (!dotState.isAir()) {
                    result.fillerBlocks.add(at);
                    result.fillerBlocks.add(dotSym);
                }

                visitedBlocks.add(dotSym);
            } else if (!state.isAir()) {
                result.fillerBlocks.add(at);
            }
        }

        result.density = (result.fillerBlocks.size() + result.symmetryPairs.size() * 2F) / ((float) result.totalCount);
        return result;
    }

    private static void checkMirrorSymmetry(BlockGetter world, Vec3i offset, BlockPos center, SymmetryResult result, Set<BlockPos> visitedBlocks) {
        BlockPos at = center.offset(offset.getX(), offset.getY(), offset.getZ());
        BlockState state = world.getBlockState(at);
        visitedBlocks.add(at);

        if (!state.isAir()) {
            result.fillerBlocks.add(at);
        }

        BlockPos dotSym = center.subtract(offset);
        BlockState dotState = world.getBlockState(dotSym);
        visitedBlocks.add(dotSym);

        if (!dotState.isAir()) {
            result.fillerBlocks.add(at);
        }
    }

    public static class SymmetryResult {

        private final int totalCount;
        private float density = 0F;
        private final Set<BiDiPair<BlockPos, BlockPos>> symmetryPairs = new HashSet<>();
        private final Set<BlockPos> fillerBlocks = new HashSet<>();

        private SymmetryResult(int totalCount) {
            this.totalCount = totalCount;
        }

        public float getDensity() {
            return density;
        }

        public Set<BiDiPair<BlockPos, BlockPos>> getSymmetryPairs() {
            return symmetryPairs;
        }

        public Set<BlockPos> getFillerBlocks() {
            return fillerBlocks;
        }
    }
}
