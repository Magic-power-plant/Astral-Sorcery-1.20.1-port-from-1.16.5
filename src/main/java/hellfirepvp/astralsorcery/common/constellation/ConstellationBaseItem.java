package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ConstellationBaseItem {

    @Nullable
    IConstellation getConstellation(ItemStack stack);

    boolean setConstellation(ItemStack stack, @Nullable IConstellation constellation);
}
