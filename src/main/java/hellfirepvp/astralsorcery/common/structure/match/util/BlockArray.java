package hellfirepvp.astralsorcery.common.structure.match.util;

import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.block.SimpleMatchableBlock;
import hellfirepvp.astralsorcery.common.structure.match.block.SimpleMatchableBlockState;
import hellfirepvp.astralsorcery.common.structure.match.structure.Structure;
import hellfirepvp.astralsorcery.common.structure.match.blockentity.MatchableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockArray implements Structure {

    private final Map<BlockPos, MatchableState> blocks = new HashMap<>();
    private final Map<BlockPos, MatchableBlockEntity<? extends BlockEntity>> blockEntities = new HashMap<>();
    private Vec3i min = new Vec3i(0, 0, 0);
    private Vec3i max = new Vec3i(0, 0, 0);

    @Nonnull
    @Override
    public Map<BlockPos, MatchableState> getContents() {
        return Collections.unmodifiableMap(blocks);
    }

    @Nonnull
    @Override
    public Map<BlockPos, ? extends MatchableBlockEntity<? extends BlockEntity>> getBlockEntities() {
        return Collections.unmodifiableMap(blockEntities);
    }

    @Override
    public Vec3i getMaximumOffset() {
        return max;
    }

    @Override
    public Vec3i getMinimumOffset() {
        return min;
    }

    public void addBlockEntity(MatchableBlockEntity<?> blockEntity, int x, int y, int z) {
        addBlockEntity(blockEntity, new BlockPos(x, y, z));
    }

    public void addBlockEntity(MatchableBlockEntity<?> blockEntity, BlockPos pos) {
        blockEntities.put(pos, blockEntity);
        updateSize(pos);
    }

    public void addBlock(BlockState state, int x, int y, int z) {
        addBlock(state, new BlockPos(x, y, z));
    }

    public void addBlock(Block block, int x, int y, int z) {
        addBlock(block, new BlockPos(x, y, z));
    }

    public void addBlock(MatchableState state, int x, int y, int z) {
        addBlock(state, new BlockPos(x, y, z));
    }

    public void addBlock(BlockState state, BlockPos pos) {
        MatchableState match = new SimpleMatchableBlockState(state);
        if (state == Blocks.AIR.defaultBlockState()) {
            match = MatchableState.AIR;
        }
        addBlock(match, pos);
    }

    public void addBlock(Block block, BlockPos pos) {
        MatchableState match = new SimpleMatchableBlock(block);
        if (block == Blocks.AIR) {
            match = MatchableState.AIR;
        }
        addBlock(match, pos);
    }

    public void addBlock(MatchableState state, BlockPos pos) {
        blocks.put(pos, state);
        updateSize(pos);
    }

    public void addAll(BlockArray other) {
        other.getContents().forEach((pos, state) -> addBlock(state, pos));
        other.getBlockEntities().forEach((pos, blockEntity) -> addBlockEntity(blockEntity, pos));
    }

    public void addBlockCube(BlockState state, int ox, int oy, int oz, int tx, int ty, int tz) {
        forAllInCube(ox, oy, oz, tx, ty, tz, (x, y, z) -> addBlock(state, x, y, z));
    }

    public void addBlockCube(MatchableState state, int ox, int oy, int oz, int tx, int ty, int tz) {
        forAllInCube(ox, oy, oz, tx, ty, tz, (x, y, z) -> addBlock(state, x, y, z));
    }

    public void forAllInCube(int ox, int oy, int oz, int tx, int ty, int tz, PositionConsumer consumer) {
        int lx = Math.min(ox, tx);
        int hx = Math.max(ox, tx);
        int ly = Math.min(oy, ty);
        int hy = Math.max(oy, ty);
        int lz = Math.min(oz, tz);
        int hz = Math.max(oz, tz);

        for (int xx = lx; xx <= hx; xx++) {
            for (int zz = lz; zz <= hz; zz++) {
                for (int yy = ly; yy <= hy; yy++) {
                    consumer.accept(xx, yy, zz);
                }
            }
        }
    }

    private void updateSize(BlockPos addedPos) {
        if (addedPos.getX() < min.getX()) {
            min = new Vec3i(addedPos.getX(), min.getY(), min.getZ());
        }
        if (addedPos.getX() > max.getX()) {
            max = new Vec3i(addedPos.getX(), max.getY(), max.getZ());
        }
        if (addedPos.getY() < min.getY()) {
            min = new Vec3i(min.getX(), addedPos.getY(), min.getZ());
        }
        if (addedPos.getY() > max.getY()) {
            max = new Vec3i(max.getX(), addedPos.getY(), max.getZ());
        }
        if (addedPos.getZ() < min.getZ()) {
            min = new Vec3i(min.getX(), min.getY(), addedPos.getZ());
        }
        if (addedPos.getZ() > max.getZ()) {
            max = new Vec3i(max.getX(), max.getY(), addedPos.getZ());
        }
    }

    @FunctionalInterface
    public interface PositionConsumer {
        void accept(int x, int y, int z);
    }
}
