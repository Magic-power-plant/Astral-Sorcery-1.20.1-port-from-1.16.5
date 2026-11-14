package hellfirepvp.astralsorcery.client.util.color;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import java.awt.*;

/** Client tint lookup for item and fluid display effects. */
public final class ItemColorUtils {
    public static int getOverlayColor(FluidStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        return 0xFFFFFFFF;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getOverlayColor(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0xFFFFFFFF;
        }
        if (stack.getItem() instanceof BlockItem) {
            BlockState state = ItemUtils.createBlockState(stack);
            if (state == null) {
                return 0xFFFFFFFF;
            }
            return Minecraft.getInstance().getBlockColors().getColor(state, null, null, 0);
        } else {
            return Minecraft.getInstance().getItemColors().getColor(stack, 0);
        }
    }

}
