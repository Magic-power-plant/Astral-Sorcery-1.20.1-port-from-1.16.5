package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface CrystalAttributeItem {

    @Nullable
    CrystalAttributes getAttributes(ItemStack stack);

    void setAttributes(ItemStack stack, @Nullable CrystalAttributes attributes);
}
