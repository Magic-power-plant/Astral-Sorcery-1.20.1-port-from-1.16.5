package hellfirepvp.astralsorcery.common.block.util;

import hellfirepvp.astralsorcery.common.structure.match.util.BlockArray;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Stack;

public class TreeDiscoverer {

    @Nonnull
    public static BlockArray findTreeAt(Level world, BlockPos at, boolean checkCorners) {
        return findTreeAt(world, at, checkCorners, -1);
    }

    @Nonnull
    public static BlockArray findTreeAt(Level world, BlockPos at, boolean checkCorners, int xzLimit) {
        int xzLimitSq = xzLimit == -1 ? -1 : xzLimit * xzLimit;
        BlockArray out = new BlockArray();
        findTree(world, at, xzLimitSq, checkCorners, out);
        return out;
    }

    private static void findTree(Level world, BlockPos at, int xzLimitSq, boolean checkCorners, BlockArray out) {
        TreeMatch foundTreeType = new TreeMatch();

        Stack<BlockPos> discoverPositions = new Stack<>();
        discoverPositions.push(at);
        while (!discoverPositions.isEmpty()) {
            BlockPos offset = discoverPositions.pop();
            if (world.isEmptyBlock(offset)) {
                continue;
            }

            BlockState foundState = world.getBlockState(offset);
            Block foundBlock = foundState.getBlock();
            if (foundTreeType.matchLog == null) {
                if (!foundState.is(BlockTags.LOGS)) {
                    return;
                }
                foundTreeType.matchLog = BlockPredicates.isBlock(foundBlock);
            } else if (foundTreeType.matchLeaf == null) {
                if (foundState.is(BlockTags.LEAVES)) {
                    foundTreeType.matchLeaf = BlockPredicates.isBlock(foundBlock);
                }
            }

            boolean successful = false;
            if (foundTreeType.matchLog.test(world, offset, foundState)) {
                successful = true;
            } else if (foundTreeType.matchLeaf != null && foundTreeType.matchLeaf.test(world, offset, foundState)) {
                successful = true;
            }

            if (successful) {
                out.addBlock(foundState, offset.getX(), offset.getY(), offset.getZ());

                if (checkCorners) {
                    for (int xx = -1; xx <= 1; xx++) {
                        for (int yy = -1; yy <= 1; yy++) {
                            for (int zz = -1; zz <= 1; zz++) {
                                BlockPos newPos = offset.offset(xx, yy, zz);
                                if((xzLimitSq == -1 || flatDistanceSq(newPos, at) <= xzLimitSq) && !out.getContents().containsKey(newPos)) {
                                    discoverPositions.push(newPos);
                                }
                            }
                        }
                    }
                } else {
                    for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                        BlockPos newPos = offset.relative(dir);
                        if((xzLimitSq == -1 || flatDistanceSq(newPos, at) <= xzLimitSq) && !out.getContents().containsKey(newPos)) {
                            discoverPositions.push(newPos);
                        }
                    }
                }
            }
        }
    }

    private static double flatDistanceSq(Vec3i from, Vec3i to) {
        double xDiff = (double) from.getX() - to.getX();
        double zDiff = (double) from.getZ() - to.getZ();
        return xDiff * xDiff + zDiff * zDiff;
    }

    private static class TreeMatch {

        private BlockPredicate matchLog;
        private BlockPredicate matchLeaf;

    }
}
