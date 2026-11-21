package hellfirepvp.astralsorcery.api.constellation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import java.util.List;

/** Read-only engraving identity and applicable item transformations. */
public interface IEngravingEffect {
    ResourceLocation getRegistryName();
    List<? extends IApplicableItemEffect> getApplicableEffects(@Nonnull ItemStack stack);
}
