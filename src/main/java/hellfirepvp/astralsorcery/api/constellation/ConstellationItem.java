package hellfirepvp.astralsorcery.api.constellation;

import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Per-stack constellation capabilities. Reads do not mutate the stack; null means absent or clear. */
public interface ConstellationItem {

    @Nullable
    IWeakConstellation getAttunedConstellation(ItemStack stack);

    boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation constellation);

    @Nullable
    IMinorConstellation getTraitConstellation(ItemStack stack);

    boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation constellation);
}
