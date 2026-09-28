package hellfirepvp.astralsorcery.common.block.ore;

import hellfirepvp.astralsorcery.common.block.base.CustomItemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class BlockStarmetal extends Block implements CustomItemBlock {

    public BlockStarmetal() {
        super(Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.METAL));
    }

}
