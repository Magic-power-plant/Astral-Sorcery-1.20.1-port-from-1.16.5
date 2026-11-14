package hellfirepvp.astralsorcery.common.util.block;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockPredicates {

    public static BlockPredicate isInTag(TagKey<Block> blockTag) {
        return (world, pos, state) -> state.is(blockTag);
    }

    public static BlockPredicate isBlock(Block... blocks) {
        Set<Block> applicable = new HashSet<>(Arrays.asList(blocks));
        return (world, pos, state) -> applicable.contains(state.getBlock());
    }

    public static BlockPredicate isState(BlockState... states) {
        Set<BlockState> applicable = new HashSet<>(Arrays.asList(states));
        return (world, pos, state) -> applicable.contains(state);
    }

    public static <T extends BlockEntity> BlockPredicate doesBlockEntityExist(T blockEntity, boolean loadBlockEntityWorldAndChunk) {
        ResourceKey<Level> dim = blockEntity.getLevel().dimension();
        BlockEntityType<?> blockEntityType = blockEntity.getType();
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();

        return (world, pos, state) -> {
            if (loadBlockEntityWorldAndChunk || srv.levelKeys().contains(dim)) {
                Level foundWorld = srv.getLevel(dim);
                if (foundWorld == null) {
                    return !loadBlockEntityWorldAndChunk;
                }
                if (!loadBlockEntityWorldAndChunk && !foundWorld.isLoaded(pos)) {
                    return true;
                }
                BlockEntity te = MiscUtils.getBlockEntityAt(foundWorld, pos, BlockEntity.class, true);
                return te != null && te.getType().equals(blockEntityType);
            }
            return true;
        };
    }

}
