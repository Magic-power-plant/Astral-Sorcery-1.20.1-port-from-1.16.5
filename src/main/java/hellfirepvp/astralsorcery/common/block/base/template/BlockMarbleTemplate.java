package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockMarbleTemplate extends Block implements CustomItemBlock {

    public BlockMarbleTemplate() {
        super(Properties.of()
                .strength(3F, 5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE));
    }

}
