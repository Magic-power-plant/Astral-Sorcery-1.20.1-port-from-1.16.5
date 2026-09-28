package hellfirepvp.astralsorcery.common.block.base.template;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

public class BlockInfusedWoodTemplate extends Block implements CustomItemBlock {

    public BlockInfusedWoodTemplate() {
        super(Properties.of()
                .strength(2.5F, 7F)
                .sound(SoundType.WOOD));
    }

}
