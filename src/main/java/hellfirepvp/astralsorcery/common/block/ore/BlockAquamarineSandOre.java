package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.template.BlockSandTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public class BlockAquamarineSandOre extends BlockSandTemplate {

    @Override
    public int getExpDrop(BlockState state, LevelReader world, RandomSource random, BlockPos pos, int fortune, int silktouch) {
        return silktouch == 0 ? fortune * random.nextInt(2, 6) : 0;
    }

}
