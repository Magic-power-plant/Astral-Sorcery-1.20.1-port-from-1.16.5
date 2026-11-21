package hellfirepvp.astralsorcery.api.constellation;

import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import java.util.Random;

/** One item transformation offered by an engraving effect. */
public interface IApplicableItemEffect {
    boolean supports(@Nonnull ItemStack stack);
    ItemStack apply(@Nonnull ItemStack stack, float percent, Random random);
}
