package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRockCrystalOre extends Block implements CustomItemBlock {

    public BlockRockCrystalOre() {
        super(Properties.of()
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader world, RandomSource random, BlockPos pos, int fortune, int silktouch) {
        return fortune * random.nextInt(8, 15);
    }

}
