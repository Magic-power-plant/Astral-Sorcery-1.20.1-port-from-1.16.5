package hellfirepvp.astralsorcery.common.item.base.client;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ItemDynamicColor {

    @OnlyIn(Dist.CLIENT)
    int getColor(ItemStack stack, int tintIndex);

}
