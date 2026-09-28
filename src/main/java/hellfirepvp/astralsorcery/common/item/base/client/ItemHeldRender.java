package hellfirepvp.astralsorcery.common.item.base.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ItemHeldRender {

    @OnlyIn(Dist.CLIENT)
    boolean renderInHand(ItemStack stack, PoseStack renderStack, float pTicks);

}
