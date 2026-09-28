package hellfirepvp.astralsorcery.common.item.base;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IConstellationFocus {
    @Nullable
    IConstellation getFocusConstellation(ItemStack stack);
}