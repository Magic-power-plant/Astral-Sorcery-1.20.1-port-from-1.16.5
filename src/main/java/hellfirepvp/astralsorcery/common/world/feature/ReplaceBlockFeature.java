package hellfirepvp.astralsorcery.common.world.feature;

import hellfirepvp.astralsorcery.common.world.feature.config.ReplaceBlockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;



public class ReplaceBlockFeature extends Feature<ReplaceBlockConfig> {

    public ReplaceBlockFeature() {
        super(ReplaceBlockConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceBlockConfig> context) {
        WorldGenLevel reader = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        net.minecraft.util.RandomSource rand = context.random();
        BlockPos pos = context.origin();
        ReplaceBlockConfig config = context.config();

        if (config.target.test(reader.getBlockState(pos), rand)) {
            return setBlockState(reader, pos, config.state);
        }
        return true;
    }

    protected boolean setBlockState(WorldGenLevel world, BlockPos pos, BlockState state) {
        return world.setBlock(pos, state, 3);
    }
}
