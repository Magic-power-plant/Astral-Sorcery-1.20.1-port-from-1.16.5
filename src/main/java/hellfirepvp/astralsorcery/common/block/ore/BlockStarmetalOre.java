package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockStarmetalOre extends Block implements CustomItemBlock {

    public BlockStarmetalOre() {
        super(BlockBehaviour.Properties.of()
                .strength(3.0F, 9.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

}
