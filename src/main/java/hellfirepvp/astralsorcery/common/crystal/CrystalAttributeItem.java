package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/** Per-stack crystal properties. Reading must not mutate the stack; null means absent or clear. */
@Deprecated
public interface CrystalAttributeItem extends hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem {

    @Override
    @Nullable
    CrystalAttributes getAttributes(ItemStack stack);

    @Override
    void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes);
}
