package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ConstellationItem {

    @Nullable
    IWeakConstellation getAttunedConstellation(ItemStack stack);

    boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation constellation);

    @Nullable
    IMinorConstellation getTraitConstellation(ItemStack stack);

    boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation constellation);
}
