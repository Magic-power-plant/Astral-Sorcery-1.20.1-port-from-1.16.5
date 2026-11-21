package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Per-stack constellation capabilities. Reads do not mutate the stack; null means absent or clear.
 * A setter returning false must leave the stack unchanged. The owning container handles synchronization.
 */
@Deprecated
public interface ConstellationItem extends hellfirepvp.astralsorcery.api.constellation.ConstellationItem {

    @Override
    @Nullable
    IWeakConstellation getAttunedConstellation(ItemStack stack);

    @Override
    boolean setAttunedConstellation(ItemStack stack, @Nullable IWeakConstellation constellation);

    @Override
    @Nullable
    IMinorConstellation getTraitConstellation(ItemStack stack);

    @Override
    boolean setTraitConstellation(ItemStack stack, @Nullable IMinorConstellation constellation);
}
