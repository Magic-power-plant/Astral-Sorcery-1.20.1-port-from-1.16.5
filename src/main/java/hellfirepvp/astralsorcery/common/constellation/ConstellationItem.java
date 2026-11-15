package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Per-stack constellation capabilities. Reads do not mutate the stack; null means absent or clear.
 * A setter returning false must leave the stack unchanged. The owning container handles synchronization.
 */
public interface ConstellationItem {

    @Nullable
    IWeakConstellation getAttunedConstellation(ItemStack stack);

    boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation constellation);

    @Nullable
    IMinorConstellation getTraitConstellation(ItemStack stack);

    boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation constellation);
}
